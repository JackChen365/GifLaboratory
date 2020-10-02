package com.cz.android.gif.sample;

import androidx.annotation.ColorInt;

import com.cz.android.gif.GifHeaderDecoder;
import com.cz.android.gif.reader.BufferedChannelReader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class GifDecoder implements Closeable {
    private static final int COLOR_TRANSPARENT_BLACK = 0x00000000;
    /**
     * GIF Disposal Method meaning take no action.
     * <p><b>GIF89a</b>: <i>No disposal specified.
     * The decoder is not required to take any action.</i></p>
     */
    static final int DISPOSAL_UNSPECIFIED = 0;
    /**
     * GIF Disposal Method meaning leave canvas from previous frame.
     * <p><b>GIF89a</b>: <i>Do not dispose.
     * The graphic is to be left in place.</i></p>
     */
    static final int DISPOSAL_NONE = 1;
    /**
     * GIF Disposal Method meaning clear canvas to background color.
     * <p><b>GIF89a</b>: <i>Restore to background color.
     * The area used by the graphic must be restored to the background color.</i></p>
     */
    static final int DISPOSAL_BACKGROUND = 2;
    /**
     * GIF Disposal Method meaning clear canvas to frame before last.
     * <p><b>GIF89a</b>: <i>Restore to previous.
     * The decoder is required to restore the area overwritten by the graphic
     * with what was there prior to rendering the graphic.</i></p>
     */
    static final int DISPOSAL_PREVIOUS = 3;
    private static final int MAX_STACK_SIZE=1 << 12;
    // allocate new pixel array
    private final GifHeaderDecoder headerDecoder=new GifHeaderDecoder();
    private byte[] pixels;
    private short[] prefix;
    private byte[] suffix;
    private byte[] pixelStack;
    private int[] mainPixels;
    private int[] currentPixels;
    /**
     * If the GIF file has a frame that the disposal method is {@link #DISPOSAL_PREVIOUS}
     * We have to go back find the frame that the disposal method is {@link #DISPOSAL_NONE} or {@link #DISPOSAL_UNSPECIFIED}
     * Here we don't want to check every time. That's why we got this class field.
     */
    private boolean hasDisposalMethod;

    private TimeTrace timeTrace=new TimeTrace();

    public void loadFile(File file) throws IOException {
        long st = System.nanoTime() * 1000L;
        headerDecoder.loadFile(file);
        int frameCount = headerDecoder.getFrameCount();
        for(int i=0;i<frameCount;i++){
            GifHeaderDecoder.GifFrame frame = headerDecoder.getFrame(i);
            if(frame.disposalMethod == DISPOSAL_PREVIOUS){
                hasDisposalMethod =true;
                break;
            }
        }
        int width = headerDecoder.getWidth();
        int height = headerDecoder.getHeight();
        int npix = width * height;
        if(null == mainPixels){
            mainPixels =new int[npix];
        }
        timeTrace.trace("loadFile");
    }

    public int[] decodeFrame(int index) throws IOException {
        GifHeaderDecoder.GifFrame frame = headerDecoder.getFrame(index);
        BufferedChannelReader reader = headerDecoder.getBufferedReader();
        int[] colorTable;
        if(null!=frame.localColorTable){
            colorTable = frame.localColorTable;
        } else {
            colorTable = headerDecoder.getGlobalColorTable();
        }
        int oldTransparentColor = colorTable[frame.transparentIndex];
        if(frame.transparency){
            colorTable[frame.transparentIndex]=0;
        }
        GifHeaderDecoder.GifFrame previousFrame=null;
        if(0 < index){
            previousFrame = headerDecoder.getFrame(index-1);
        }
        int[] imageData = readBasedImageData(index,reader,frame,previousFrame, colorTable, frame.disposalMethod);
        colorTable[frame.transparentIndex]=oldTransparentColor;
        return imageData;
    }

    private int[] readBasedImageData(int frameIndex,BufferedChannelReader reader, GifHeaderDecoder.GifFrame currentFrame,
                                     GifHeaderDecoder.GifFrame previousFrame,int[] colorTable, int disposalMethod) throws IOException {
        int width = headerDecoder.getWidth();
        int[] previousBitmap = currentPixels;
        int[] dest = mainPixels;

        // clear all pixels when meet first frame and drop prev image from last loop
        if (previousFrame == null) {
            previousBitmap = null;
            Arrays.fill(dest, COLOR_TRANSPARENT_BLACK);
        }

        // clear all pixels when dispose is 3 but previousImage is null.
        // When DISPOSAL_PREVIOUS and previousImage didn't be set, new frame should draw on
        // a empty image
        if (previousFrame != null && previousFrame.disposalMethod == DISPOSAL_PREVIOUS && previousBitmap == null) {
            Arrays.fill(dest, COLOR_TRANSPARENT_BLACK);
        }
        // fill in starting image contents based on last image's dispose code
        if (null != previousFrame && disposalMethod > DISPOSAL_UNSPECIFIED) {
            // We don't need to do anything for DISPOSAL_NONE, if it has the correct pixels so will our
            // mainScratch and therefore so will our dest array.
            if (previousFrame.disposalMethod == DISPOSAL_BACKGROUND) {
                // Start with a canvas filled with the background color
                @ColorInt int backgroundColor = COLOR_TRANSPARENT_BLACK;
                if (!currentFrame.transparency) {
                    byte backgroundColorIndex = headerDecoder.getBackgroundColorIndex();
                    backgroundColor = headerDecoder.getBackgroundColor();
                    if (currentFrame.localColorTable != null && backgroundColorIndex == currentFrame.transparentIndex) {
                        backgroundColor = COLOR_TRANSPARENT_BLACK;
                    }
                }
                // The area used by the graphic must be restored to the background color.
                int topPosition = previousFrame.top;
                int leftPosition = previousFrame.left;
                int imageWidth = previousFrame.imageWidth;
                int imageHeight = previousFrame.imageHeight;
                int topLeft = topPosition * width + leftPosition;
                int bottomLeft = topLeft + imageHeight * width;
                for (int left = topLeft; left < bottomLeft; left += width) {
                    int right = left + imageWidth;
                    for (int pointer = left; pointer < right; pointer++) {
                        dest[pointer] = backgroundColor;
                    }
                }
            } else if (previousFrame.disposalMethod == DISPOSAL_PREVIOUS && previousBitmap != null) {
                // Start with the previous frame
                dest = previousBitmap;
            }
        }
        timeTrace.trace("frame:"+frameIndex+" initialize previous.");
        if(null!=currentFrame.imageDescriptor){
            reader.position(currentFrame.imageDescriptor.start);
        }
        int top=currentFrame.top;
        int left=currentFrame.left;
        int imageWidth = currentFrame.imageWidth;
        int imageHeight = currentFrame.imageHeight;
        byte[] pixels = decodeCodeTable(reader, imageWidth, imageHeight);
        timeTrace.trace("frame:"+frameIndex+" decode code table.");
        // copy each source line to the appropriate place in the destination
        int pass = 1;
        int inc = 8;
        int iline = 0;
        byte transparentColorIndex = -1;
        for (int i = 0; i < imageHeight; i++) {
            int line = i + top;
            if (currentFrame.interlace) {
                if (iline >= imageHeight) {
                    pass++;
                    switch (pass) {
                        case 2 :
                            iline = 4;
                            break;
                        case 3 :
                            iline = 2;
                            inc = 4;
                            break;
                        case 4 :
                            iline = 1;
                            inc = 2;
                    }
                }
                line = iline;
                iline += inc;
            }
            int k = line * width;
            int pixelIndex = k + left; // start of line in dest
            int dlim = pixelIndex + imageWidth; // end of dest line
            if (k + width < dlim) {
              dlim = k + width; // past dest edge
            }
            int sx = i * imageWidth; // start of line in source
            while (pixelIndex < dlim) {
                // map color and insert in destination
                byte byteCurrentColorIndex = pixels[sx];
                int currentColorIndex = byteCurrentColorIndex & 0xff;
                if(currentColorIndex != transparentColorIndex){
                    int color = colorTable[currentColorIndex];
                    if(color != COLOR_TRANSPARENT_BLACK){
                        dest[pixelIndex] = color;
                    } else {
                        transparentColorIndex = byteCurrentColorIndex;
                    }
                }
                pixelIndex++;
                sx++;
            }
          }
        // Keep the drawing bitmap.
        if(hasDisposalMethod && (currentFrame.disposalMethod ==DISPOSAL_UNSPECIFIED|| currentFrame.disposalMethod == DISPOSAL_NONE)){
            //Save the current bitmap.
            currentPixels = mainPixels;
        }
        timeTrace.trace("frame:"+frameIndex+" process pixels.");
        return mainPixels;
    }

    private byte[] decodeCodeTable(BufferedChannelReader reader, int imageWidth, int imageHeight) throws IOException {
        int width = getWidth();
        int height = getHeight();
        int npix = null==pixels ? width*height : imageWidth * imageHeight;
        if(null==pixels||pixels.length<npix){
            pixels = new byte[npix]; // allocate new pixel array
        }
        if(null==prefix){
            prefix = new short[MAX_STACK_SIZE];
        }
        if(null==suffix){
            suffix = new byte[MAX_STACK_SIZE];
        }
        if(null==pixelStack){
            pixelStack = new byte[MAX_STACK_SIZE + 1];
        }
        //LZW Minimum Code Size
        int index=0;
        int minLZWSize=reader.readByteUnsigned();
        int codeSize=minLZWSize+1;
        int clearCode= 1 << minLZWSize;
        int eof=clearCode+1;
        int codeMask=(1 << codeSize) - 1;
        for(int i=0;i<clearCode;i++){
            suffix[i]= (byte) i;
        }
        //The code index is the index of the code table.
        int codeIndex = eof + 1;
        //Stack index is actually the stack size. We pop each element reversely
        int blockSize=reader.readByteUnsigned();
        int stackIndex=pixelStack.length;
        int pixelIndex=0;
        int bits = 0;
        int oldCode = 0;
        int first = 0;
        int code = 0;
        int datum = 0;
        while(index < npix){
            //consume all the pixels in the stack.
            if(stackIndex<pixelStack.length){
                int stackSize = pixelStack.length - stackIndex;
                System.arraycopy(pixelStack,stackIndex,pixels,pixelIndex,stackSize);
                pixelIndex+=stackSize;
                stackIndex=pixelStack.length;
            }
            while(bits < codeSize){
                if(0 == blockSize){
                    blockSize = reader.readByte() & 0xFF;
                    if(0 >= blockSize){
                        break;
                    }
                }
                datum |= (reader.readByte() & 0xFF) << bits;
                blockSize --;
                bits += 8;
            }
            code  = datum & codeMask;
            datum >>= codeSize;
            bits -= codeSize;

            if(code > codeIndex || code == eof){
                break;
            } else if(code == clearCode){
                codeSize=minLZWSize+1;
                codeMask=(1 << codeSize) - 1;
                codeIndex = clearCode+2;
                //Here the old code would be the clear code.
                oldCode = code;
                continue;
            } else if (oldCode == clearCode) {
                //When we clear the code table. What should we do.
                pixelStack[--stackIndex] = suffix[code];
                oldCode = code;
                first = code;
                continue;
            }
            int curCode = code;
            if (code == codeIndex) {
                pixelStack[--stackIndex] = (byte) first;
                code = oldCode;
            }
            while(code >= clearCode){
                pixelStack[--stackIndex] = suffix[code];
                code =  prefix[code];
            }
            first = (suffix[code]) & 0xFF;
            pixelStack[--stackIndex] = (byte) first;
            if (codeIndex < MAX_STACK_SIZE){
                prefix[codeIndex] = (short) oldCode;
                suffix[codeIndex] = (byte) first;
                codeIndex++;
                if((0 == (codeIndex & codeMask)) && (codeSize < 12)){
                    codeSize++;
                    codeMask = (1 << codeSize) - 1;
                }
            }
            oldCode = curCode;
        }
        if(0 != stackIndex){
            System.arraycopy(pixelStack,stackIndex,pixels,pixelIndex,pixelStack.length - stackIndex);
        }
        Arrays.fill(pixels,pixelIndex,npix, (byte) 0);
        return pixels;
    }

    public TimeTrace getTimeTrace() {
        return timeTrace;
    }

    public int getFrameCount(){
        return headerDecoder.getFrameCount();
    }

    public String getVersion() {
        return headerDecoder.getVersion();
    }

    public int getWidth() {
        return headerDecoder.getWidth();
    }

    public int getHeight() {
        return headerDecoder.getHeight();
    }

    public int[] getGlobalColorTable() {
        return headerDecoder.getGlobalColorTable();
    }

    public float getAspectRatio() {
        return headerDecoder.getAspectRatio();
    }

    public short getLoopCount(){
        return headerDecoder.getLoopCount();
    }

    @Override
    public void close() throws IOException {
        if(null!=headerDecoder){
            headerDecoder.close();
        }
    }

}
