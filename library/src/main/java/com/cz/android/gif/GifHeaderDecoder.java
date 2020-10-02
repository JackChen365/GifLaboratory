package com.cz.android.gif;

import android.util.Log;

import androidx.annotation.ColorInt;

import com.cz.android.gif.reader.BufferedChannelReader;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * The basic GIF file reader. Only decode the basic information.
 *
 * We use the following methods to decode the file.
 * @see #readHead(BufferedChannelReader)
 * @see #readLogicalScreenDescriptor(BufferedChannelReader)
 * @see #readContent(BufferedChannelReader)
 *
 * Inside the readContent. We process the image descriptor and image extension.
 * It is faster for us to know the information of the GIF file.
 * After decode you are able to know the dimension and the all the frames.
 *
 * After I did a bunch of reseach. I have to admit that it is not easy to decode the GIF file.
 * I can see that even Glide's GIF decoder are not support all the features. And it really need a lot of time to finish it.
 * So if you want to complete all the feature of GIF file. good luck.
 *
 *
 */
public class GifHeaderDecoder implements Closeable {
    private static final String TAG="GifHeaderDecoder";
    private static final boolean DEBUG =false;
    /**
     * The minimum frame delay in hundredths of a second.
     */
    static final int MIN_FRAME_DELAY = 2;
    /**
     * The default frame delay in hundredths of a second.
     * This is used for GIFs with frame delays less than the minimum.
     */
    static final int DEFAULT_FRAME_DELAY = 10;
    private List<GifFrame> frameList =new ArrayList<>();
    private BufferedChannelReader bufferedReader;
    private String version;
    private int width;
    private int height;

    private int[] globalColorTable;
    private byte colorResolution;
    private byte sortFlag;

    private short loopCount;
    private byte backgroundColorIndex;
    private float aspectRatio;

    // allocate new pixel array
    private int frameIndex;

    public void loadFile(File file) throws IOException {
        FileInputStream fileInputStream=new FileInputStream(file);
        frameIndex = 0;
        bufferedReader = new BufferedChannelReader(fileInputStream.getChannel());
        //Step1: determine whether this is a gif file.
        readHead(bufferedReader);
        //Step2: The logical screen descriptor.
        readLogicalScreenDescriptor(bufferedReader);
        //Step3: Start readByte content.
        readContent(bufferedReader);
    }

    private void readHead(BufferedChannelReader bufferedReader) throws IOException {
        String head = bufferedReader.readString(6, Charset.defaultCharset());
        if(head.startsWith("GIF")){
            String version = head.substring(3);
            if(version.equals("87a")||version.equals("89a")){
                println(version);
            }
        }
    }

    private void readLogicalScreenDescriptor(BufferedChannelReader bufferedReader) throws IOException {
        println("ReadLogicalScreenDescriptor=============================\n");
//    i) Logical Screen Width - Width, in pixels, of the Logical Screen
//    where the images will be rendered in the displaying device.
        width = bufferedReader.readShortLe();
        height = bufferedReader.readShortLe();

        byte packed = bufferedReader.readByte();
        //11111111
        byte globalColorTableFlag = (byte) ((packed>>7) & 0x01);
        colorResolution  = (byte) ((packed>>4)&0x07);
        //turn 11110111 to 11110 by move to left 3 bit.
        sortFlag  = (byte) ((packed>>3) & 0x01);
        //The hex 0xf8 equal to binary value:11111000
        int globalColorTableSize=  (packed & 0x07) & 0xFF;

        int colorTableSize=1<<(globalColorTableSize+1);
        backgroundColorIndex=bufferedReader.readByte();

        byte aspectRatioValue=bufferedReader.readByte();
        aspectRatio=(aspectRatioValue+15)/64;
        if(1==globalColorTableFlag){
            println("ReadGlobalColorTable=============================\n");
            globalColorTable = readColorTable(bufferedReader, colorTableSize);
        }
    }

    private int[] readColorTable(BufferedChannelReader bufferedReader, int colorTableSize) throws IOException {
        //Du to some weird problems. Like the transparent index was 255...
        //Here we are not going to just use the color table size.
        int[] globalColorTable=new int[256];
        for(int i=0;i<colorTableSize;i++){
            int r=bufferedReader.readByteUnsigned();
            int g = bufferedReader.readByteUnsigned();
            int b = bufferedReader.readByteUnsigned();
            globalColorTable[i]=0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return globalColorTable;
    }

    private void readGraphicControlExtension(BufferedChannelReader reader,GifFrame frame) throws IOException {
        println("ReadGraphicControlExtension=============================");
        //Block Size                    1 Byte
        reader.skip(1);
        //Packed field
        int packed = reader.readByteUnsigned();
        //Delay Time
        short delayInHundredthsOfASecond = reader.readShortLe();
        if (delayInHundredthsOfASecond < MIN_FRAME_DELAY) {
            delayInHundredthsOfASecond = DEFAULT_FRAME_DELAY;
        }
        frame.delay = delayInHundredthsOfASecond * 10;
        //Transparent Color Index
        frame.transparentIndex = reader.readByteUnsigned();
        //Reserved                      3 Bits
//        byte reserved= (byte) (packed >> 5);
        //Disposal Method               3 Bits
        frame.disposalMethod = packed >> 2 & 0x3;
        //User Input Flag               1 Bit
        int userInputFlag = packed >> 1 & 0x1;
        //Transparent Color Flag        1 Bit
        frame.transparency = 1 == (packed & 0x1);
        //Skip the block terminator.
        reader.skip(1);
    }

    private void readPlainTextExtension(BufferedChannelReader reader) throws IOException {
        //Plain Text Label
        println("ReadPlainTextExtension=============================\n");
        //Block Size
        int plainTextBlockSize=reader.readByteUnsigned();

        //The Text Grid Left Position
        short textGridLeftPosition=reader.readShortLe();
        //The Text Grid Top Position
        short textGridTopPosition=reader.readShortLe();
        println("\tBlock Size:"+plainTextBlockSize+" Grid Left Position:"+textGridLeftPosition+" Grid Top Position:"+textGridTopPosition+"\n");

        //Text Grid Width
        short gridWidth=reader.readShortLe();
        //Text Grid Height
        short gridHeight=reader.readShortLe();

        //Character Cell Width
        int characterCellWidth=reader.readByteUnsigned();
        //Character Cell Height
        int characterCellHeight=reader.readByteUnsigned();
        println("\tGrid Width:"+gridWidth+" Grid Height:"+gridHeight+
                " Character Cell Width:"+characterCellWidth+
                " Character Cell Height:"+characterCellHeight+"\n");

        //Text Foreground Color Index
        int textForegroundColorIndex = reader.readByteUnsigned();
        //Text Background Color Index
        int  textBackgroundColorIndex = reader.readByteUnsigned();
        println("\tText Foreground Color Index:"+textForegroundColorIndex+" Text Background Color Index:"+textBackgroundColorIndex+"\n");
    }

    private void readApplicationExtension(BufferedChannelReader reader) throws IOException {
        println("ReadApplicationExtension=============================\n");
        //Block Size                    1 Byte
        long position = reader.position();
        byte blockSize=reader.readByte();
        println("\tBlock Size:"+blockSize+"\n");
        //Application Identifier        8 Bytes
        String applicationIdentifier=reader.readString(8,Charset.defaultCharset());
        println("\tApplication Identifier:"+applicationIdentifier+"\n");

        //Appl. Authentication Code     3 Bytes
        String authenticationCode=reader.readString(3,Charset.defaultCharset());
        println("\tAuthentication Code:"+authenticationCode+"\n");

        if("NETSCAPE".equalsIgnoreCase(applicationIdentifier)&&"2.0".equalsIgnoreCase(authenticationCode)){
            //process Netscape2.0 extension
            loopCount=reader.readShortLe();
            reader.skip(1);
            //Application Data              Data Sub-blocks
            println("\tApplication Data Sub-blocks:"+loopCount+"\n");
        } else {
            //XMP packed or others.
            int xmpBlockSize=reader.readByteUnsigned();
            int blockTotalSize=xmpBlockSize;
            while(0 != xmpBlockSize){
                reader.skip(xmpBlockSize);
                xmpBlockSize=reader.readByteUnsigned();
                blockTotalSize += xmpBlockSize;
            }
            long end = reader.position();
            println("blockTotalSize:"+blockTotalSize+" totalCount:"+(end-position));
        }
    }

    private void readExtension(BufferedChannelReader reader,GifFrame gifFrame) throws IOException {
        int separator = reader.readByteUnsigned();
        switch (separator){
            case 0xF9:{
                // 23. Graphic Control Extension.
                //i) Graphic Control Label - Identifies the current block as a Graphic Control Extension. This field contains the fixed value 0xF9.
                println("Start readByte the Graphic Control Extension.");
                readGraphicControlExtension(reader,gifFrame);
                break;
            }
            case 0xFE:{
                // 24. Comment Extension.
                //i) Comment Label - Identifies the block as a Comment Extension.This field contains the fixed value 0xFE.
                println("Start readByte the Comment Extension.\n");
                skipDataBlock(reader);
                break;
            }
            case 0x01:{
                // 25. Plain Text Extension.
                //i) Plain Text Label - Identifies the current block as a Plain Text Extension. This field contains the fixed value 0x01.
                println("Start readByte the Plain Text Extension.\n");
                readPlainTextExtension(reader);
                break;
            }
            case 0xFF:{
                // 26. Application Extension Label.
                //ii) Application Extension Label - Identifies the block as an Application Extension. This field contains the fixed value 0xFF.
                println("Start readByte the Application Extension Label.\n");
                readApplicationExtension(reader);
                break;
            }
        }
    }

    private void readImageDescriptor(BufferedChannelReader reader,GifFrame frame) throws IOException {
        println("ReadImageDescriptor=============================");
        short imageLeftPosition = reader.readShortLe();
        short imageTopPosition = reader.readShortLe();
        frame.left = imageLeftPosition;
        frame.top = imageTopPosition;
        frame.imageWidth= reader.readShortLe();
        frame.imageHeight= reader.readShortLe();

        int packed=reader.readByteUnsigned();
        println("\tImageLeftPosition:"+imageLeftPosition+" imageTopPosition: "+imageTopPosition+
                " imageWidth:"+frame.imageWidth+" imageHeight:"+frame.imageHeight);
//<Packed Fields>  =
//                Local Color Table Flag        1 Bit
//                Interlace Flag                1 Bit
//                Sort Flag                     1 Bit
//                Reserved                      2 Bits
//                Size of Local Color Table     3 Bits

        println("\tpacked:"+packed+" "+Integer.toBinaryString(packed));
        //Local Color Table Flag
        int localColorTableFlag=packed>>7;
        //Interlace Flag the value 01000000 equal to the hex value: 0x40
        frame.interlace = 1 == (packed >> 6 & 0x1);
        //Sort Flag the value 00100000 equal to the hex value: 0x40
        int sortFlag=packed>>5 & 0x1;
        //Skip the reserved bits.
        //Size of Local Color Table 0x07 equal to the binary value:0000 0111
        int localColorTableSize=packed&0x07;
        int colorTableSize= 1 << (localColorTableSize+1);

        println("\tlocalColorTableFlag:"+localColorTableFlag+" "+
                "interlaceFlag:"+frame.interlace+" "+"sortFlag:"+sortFlag+" "+
                "localColorTableSize:"+localColorTableSize);
        if(1==localColorTableFlag){
            frame.localColorTable=readColorTable(reader,colorTableSize);
        }
    }

    private void skipDataBlock(BufferedChannelReader reader) throws IOException {
        println("skipDataBlock=============================");
        //LZW Minimum Code Size
        int blockSize=reader.readByteUnsigned();
        while(0 != blockSize){
            reader.skip(blockSize);
            blockSize=reader.readByteUnsigned();
        }
    }

    private void readContent(BufferedChannelReader reader) throws IOException {
        int separator;
        GifFrame frame=new GifFrame();
        while(true) {
            separator=reader.readByteUnsigned();
            switch (separator) {
                case 0x2C: {
                    //i) Image Separator - Identifies the beginning of an Image Descriptor. This field contains the fixed value 0x2C.
                    println("Start readByte the Image Separator.\n");
                    frame.index=frameIndex++;
                    frame.imageDescriptor=new GifDataBlock();
                    readImageDescriptor(reader,frame);
                    frame.imageDescriptor.start=reader.position();
                    reader.skip(1);
                    skipDataBlock(reader);
                    frame.imageDescriptor.end=reader.position();
                    frameList.add(frame);
                    frame = new GifFrame();
                    break;
                }
                case 0x21: {
                    println("Start readByte the Extension.\n");
                    readExtension(reader,frame);
                    break;
                }
                case 0x3B: {
                    // 27. Trailer.
                    //i)a. Description. This block is a single-field block indicating the end of the GIF Data Stream.  It contains the fixed value 0x3B.
                    println("The Trailer. this is the end of the file.\n");
                    return;
                }
                case 0: // bad byte, but keep going and see what happens break;
                    break;
                default:
                    println("Unknown code:"+separator+"\n");
                    return;
            }
        }
    }

    private void println(String message) {
        if(DEBUG){
            Log.i(TAG,message);
        }
    }

    public int getFrameCount(){
        return frameList.size();
    }

    public GifFrame getFrame(int index){
        return frameList.get(index);
    }

    public String getVersion() {
        return version;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getGlobalColorTable() {
        return globalColorTable;
    }

    public byte getColorResolution() {
        return colorResolution;
    }

    public byte getSortFlag() {
        return sortFlag;
    }

    public short getLoopCount() {
        return loopCount;
    }

    public byte getBackgroundColorIndex() {
        return backgroundColorIndex;
    }

    public int getBackgroundColor(){
        return globalColorTable[backgroundColorIndex];
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public List<GifFrame> getFrameList() {
        return frameList;
    }

    public int getDelayTime(int index) {
        int frameCount = getFrameCount();
        if(index >= frameCount){
            System.out.println();
        }
        GifFrame gifFrame = frameList.get(index);
        return gifFrame.delay;
    }

    public BufferedChannelReader getBufferedReader() {
        return bufferedReader;
    }

    @Override
    public void close() {
        frameList.clear();
        if(null!=bufferedReader){
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bufferedReader=null;
        }
    }

    public static class GifDataBlock {
        public long start;
        public long end;
    }

    /**
     * A gif frame is one frame inside the file.
     * We use this class to decode image lazily.
     */
    public static class GifFrame{
        /**
         * GIF Disposal Method meaning take no action.
         * <p><b>GIF89a</b>: <i>No disposal specified.
         * The decoder is not required to take any action.</i></p>
         */
        public static final int DISPOSAL_UNSPECIFIED = 0;
        /**
         * GIF Disposal Method meaning leave canvas from previous frame.
         * <p><b>GIF89a</b>: <i>Do not dispose.
         * The graphic is to be left in place.</i></p>
         */
        public static final int DISPOSAL_NONE = 1;
        /**
         * GIF Disposal Method meaning clear canvas to background color.
         * <p><b>GIF89a</b>: <i>Restore to background color.
         * The area used by the graphic must be restored to the background color.</i></p>
         */
        public static final int DISPOSAL_BACKGROUND = 2;
        /**
         * GIF Disposal Method meaning clear canvas to frame before last.
         * <p><b>GIF89a</b>: <i>Restore to previous.
         * The decoder is required to restore the area overwritten by the graphic
         * with what was there prior to rendering the graphic.</i></p>
         */
        public static final int DISPOSAL_PREVIOUS = 3;

        public int index;
        /**
         * The frame data block.
         */
        public GifDataBlock imageDescriptor;
        /**
         * The image location.
         */
        public int left, top;
        /**
         * The image dimension.
         */
        public int imageWidth,imageHeight;
        /**
         * Control Flag.
         */
        public boolean interlace;
        /**
         * Control Flag.
         */
        public boolean transparency;
        /**
         * Disposal Method.
         */
        public int disposalMethod;
        /**
         * Transparency Index.
         */
        public int transparentIndex;
        /**
         * Delay, in milliseconds, to next frame.
         */
        public int delay;
        /**
         * Local Color Table.
         */
        @ColorInt
        public int[] localColorTable;
    }
}
