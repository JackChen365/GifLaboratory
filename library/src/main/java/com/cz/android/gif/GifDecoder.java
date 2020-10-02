package com.cz.android.gif;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.ColorInt;
import com.cz.android.gif.reader.BufferedChannelReader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * The GIF file decoder.
 * Inside this class we have a {@link GifHeaderDecoder} Which help us to decode all the basic information.
 * This class use {@link com.cz.android.gif.glide.GifFrame} to decode the image lazily.
 *
 * @see #loadFile(File) From a exist file load the Image file.
 * @see #getWidth() The basic image width.
 * @see #getHeight() The image height
 * @see #decodeFrame(int) decode a one frame.
 * @see #getFrameCount() Total frame size.
 * @see #getDelayTime(int) The frame delayed time.
 * @see #getGlobalColorTable() The global color table.
 */
public class GifDecoder implements Closeable {
    private static final String TAG="GifDecoder";
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
    private int[] currentPixels;
    private Bitmap previousBitmap;
    private Bitmap currentBitmap;
    /**
     * If the GIF file has a frame that the disposal method is {@link #DISPOSAL_PREVIOUS}
     * We have to go back find the frame that the disposal method is {@link #DISPOSAL_NONE} or {@link #DISPOSAL_UNSPECIFIED}
     * Here we don't want to check every time. That's why we got this class field.
     */
    private boolean hasDisposalMethod;

    public void loadFile(File file) throws IOException {
        headerDecoder.loadFile(file);
        int frameCount = headerDecoder.getFrameCount();
        for(int i=0;i<frameCount;i++){
            GifHeaderDecoder.GifFrame frame = headerDecoder.getFrame(i);
            if(frame.disposalMethod == DISPOSAL_PREVIOUS){
                hasDisposalMethod =true;
                break;
            }
        }
    }

    /**
     * Decode an image frame by the index.
     * @param index
     * @return
     */
    public Bitmap decodeFrame(int index) {
        GifHeaderDecoder.GifFrame currentFrame = headerDecoder.getFrame(index);
        BufferedChannelReader reader = getBufferedReader();
        int[] colorTable;
        if(null!=currentFrame.localColorTable){
            colorTable = currentFrame.localColorTable;
        } else {
            colorTable = headerDecoder.getGlobalColorTable();
        }
        int oldTransparentColor = colorTable[currentFrame.transparentIndex];
        if(currentFrame.transparency){
            colorTable[currentFrame.transparentIndex]=0;
        }
        Bitmap bitmap=null;
        try {
            if(null!=currentFrame.imageDescriptor){
                reader.position(currentFrame.imageDescriptor.start);
            }
            GifHeaderDecoder.GifFrame previousFrame=null;
            if(0 < index){
                previousFrame = headerDecoder.getFrame(index-1);
            }
            bitmap = readBasedImageData(reader, currentFrame,previousFrame, colorTable, currentFrame.disposalMethod);
        } catch (IOException e) {
            e.printStackTrace();
        }
        colorTable[currentFrame.transparentIndex]=oldTransparentColor;
        return bitmap;
    }

    /**
     * Read the basic image data by the given frame.
     * @param reader
     * @param currentFrame
     * @param previousFrame
     * @param colorTable
     * @param disposalMethod
     * @return
     * @throws IOException
     */
    private Bitmap readBasedImageData(BufferedChannelReader reader, GifHeaderDecoder.GifFrame currentFrame,
                                      GifHeaderDecoder.GifFrame previousFrame, int[] colorTable, int disposalMethod) throws IOException {

        int width = headerDecoder.getWidth();
        int height = headerDecoder.getHeight();
        int npix = width * height;
        if(null == currentPixels){
            currentPixels = new int[npix];
        }
        Bitmap previous = previousBitmap;
        // clear all pixels when meet first frame and drop prev image from last loop
        if (previousFrame == null) {
            previous = null;
            Arrays.fill(currentPixels, COLOR_TRANSPARENT_BLACK);
        }
        // clear all pixels when dispose is 3 but previousImage is null.
        // When DISPOSAL_PREVIOUS and previousImage didn't be set, new frame should draw on
        // a empty image
        if (previousFrame != null && previousFrame.disposalMethod == DISPOSAL_PREVIOUS && previous == null) {
            Arrays.fill(currentPixels, COLOR_TRANSPARENT_BLACK);
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
                        currentPixels[pointer] = backgroundColor;
                    }
                }
            } else if (previousFrame.disposalMethod == DISPOSAL_PREVIOUS && previous != null) {
                // Start with the previous frame
                previous.getPixels(currentPixels, 0, width, 0, 0, width, height);
            }
        }
        int top=currentFrame.top;
        int left=currentFrame.left;
        int imageWidth = currentFrame.imageWidth;
        int imageHeight = currentFrame.imageHeight;
        boolean interlace = currentFrame.interlace;
        byte[] pixels = decodeCodeTable(reader, imageWidth, imageHeight);
        // copy each source line to the appropriate place in the destination
        int pass = 1;
        int inc = 8;
        int iline = 0;
        byte transparentColorIndex = -1;
        for (int i = 0; i < imageHeight; i++) {
          int line = i;
          if (interlace) {
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
          line += top;
          if (line < height) {
            int k = line * width;
            int pixelIndex = k + left; // start of line in dest
            int dlim = pixelIndex + imageWidth; // end of dest line
            if ((k + width) < dlim) {
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
                        if(pixelIndex >= currentPixels.length){
                            Log.i(TAG,"currentPixels:"+currentPixels.length+" pixelIndex:"+pixelIndex+" npix:"+npix+" width:"+width+" height:"+height+" imageWidth:"+imageWidth+" imageHeight:"+imageHeight);
                        }
                        currentPixels[pixelIndex] = color;
                    } else {
                        transparentColorIndex = byteCurrentColorIndex;
                    }
                }
                pixelIndex++;
                sx++;
            }
          }
        }
        // fill in starting image contents based on last image's dispose code
//        Values :    0 -   No disposal specified. The decoder is
//        not required to take any action.
//        1 -   Do not dispose. The graphic is to be left
//        in place.
//        2 -   Restore to background color. The area used by the
//        graphic must be restored to the background color.
//        3 -   Restore to previous. The decoder is required to
//        restore the area overwritten by the graphic with
//        what was there prior to rendering the graphic.
//        4-7 -    To be defined.
        Bitmap drawingBitmap=currentBitmap;
        if(null==drawingBitmap){
            drawingBitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        }
        drawingBitmap.setPixels(currentPixels,0,width,0,0,width,height);
        currentBitmap = drawingBitmap;
        // Keep the drawing bitmap.
        if(hasDisposalMethod && (currentFrame.disposalMethod ==DISPOSAL_UNSPECIFIED|| currentFrame.disposalMethod == DISPOSAL_NONE)){
            //Save the current bitmap.
            previousBitmap = drawingBitmap.copy(Bitmap.Config.ARGB_8888,true);
        }
        return drawingBitmap;
    }

    /**
     * Decode the code table.
     * @param reader
     * @param imageWidth
     * @param imageHeight
     * @return
     * @throws IOException
     */
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
        int stackIndex=0;
        int pixelIndex=0;
        int bits = 0;
        int oldCode = 0;
        int first = 0;
        int code = 0;
        int datum = 0;
        while(index < npix){
            //consume all the pixels in the stack.
            while(0 != stackIndex){
                pixels[pixelIndex] = pixelStack[--stackIndex];
                pixelIndex++;
            }
            if(bits < codeSize){
                if(0 == blockSize){
                    blockSize = reader.readByteUnsigned();
                    if(0 >= blockSize){
                        break;
                    }
                }
                datum |= (reader.readByteUnsigned()) << bits;
                blockSize --;
                bits += 8;
                continue;
            }
            code  = datum & codeMask;
            datum >>= codeSize;
            bits -= codeSize;

            if(code > codeIndex || code == eof){
                break;
            } else if(code == clearCode){
//                System.out.println("codeSize1:"+codeSize+" pixelIndex:"+pixelIndex+" blockIndex:"+reader.position());
                codeSize=minLZWSize+1;
                codeMask=(1 << codeSize) - 1;
                codeIndex = clearCode+2;
                //Here the old code would be the clear code.
                oldCode = code;
                continue;
            } else if (oldCode == clearCode) {
                //When we clear the code table. What should we do.
                pixelStack[stackIndex++] = suffix[code];
                oldCode = code;
                first = code;
                continue;
            }
            int curCode = code;
            if (code == codeIndex) {
                pixelStack[stackIndex++] = (byte) first;
                code = oldCode;
            }
            while(code >= clearCode){
//                System.out.println("stackIndex:"+stackIndex+" code:"+code);
                pixelStack[stackIndex++] = suffix[code];
                code =  prefix[code];
            }
            first = ((int) suffix[code]) & 0xFF;
            pixelStack[stackIndex++] = (byte) first;
            if (codeIndex < MAX_STACK_SIZE){
                prefix[codeIndex] = (short) oldCode;
                suffix[codeIndex] = (byte) first;
                codeIndex++;
                if((0 == (codeIndex & codeMask)) && (codeSize < 12)){
                    //                System.out.println("index:"+index+" codeIndex:"+codeIndex+" codeSize:"+codeSize+" codeMask:"+codeMask);
                    codeSize++;
                    codeMask = (1 << codeSize) - 1;
                }
            }
            oldCode = curCode;
        }
        while(0 != stackIndex){
            pixels[pixelIndex++] = pixelStack[--stackIndex];
        }
        Arrays.fill(pixels, pixelIndex, npix, (byte) COLOR_TRANSPARENT_BLACK);
        return pixels;
    }

    /**
     * Return the frame count.
     * @return
     */
    public int getFrameCount(){
        return headerDecoder.getFrameCount();
    }

    /**
     * Return the version of the file.
     * @return
     */
    public String getVersion() {
        return headerDecoder.getVersion();
    }

    /**
     * Return the logical width of the image.
     * @return
     */
    public int getWidth() {
        return headerDecoder.getWidth();
    }

    /**
     * Return the logical height of the image.
     * @return
     */
    public int getHeight() {
        return headerDecoder.getHeight();
    }

    /**
     * Return the global color table.
     * @return
     */
    public int[] getGlobalColorTable() {
        return headerDecoder.getGlobalColorTable();
    }

    public float getAspectRatio() {
        return headerDecoder.getAspectRatio();
    }

    /**
     * Return the buffered reader. this is for visualize or somethings else.
     * So you could use the same file reader to save the memory.
     * @return
     */
    public BufferedChannelReader getBufferedReader() {
        return headerDecoder.getBufferedReader();
    }

    /**
     * Return the loop count. if it is equal to zero. it means loop infinite.
     * @return
     */
    public short getLoopCount(){
        return headerDecoder.getLoopCount();
    }

    /**
     * Return the delay time of each frame.
     * @param index
     * @return
     */
    public int getDelayTime(int index) {
        return headerDecoder.getDelayTime(index);
    }

    /**
     * Close the file stream.
     * @throws IOException
     */
    @Override
    public void close(){
        currentPixels = null;
        currentBitmap = null;
        previousBitmap = null;
        if(null!=headerDecoder){
            headerDecoder.close();
        }
    }

}
