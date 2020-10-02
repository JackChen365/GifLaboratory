package com.cz.android.gif.gif;

import com.cz.android.gif.GifHeaderDecoder;
import com.cz.android.gif.reader.BufferedChannelReader;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Test GIF file decoding.
 * Unfortunately. both my implementation and the ImageMagick implementation has some problems.
 * <pre>
 *     //We can not just jump out of the code block.
 *      if (codeIndex >= MaxStackSize)
 *                 break;
 *             first = suffix[code] & 0xff;
 *             pixelStack[stackIndex++] = (byte) first;
 *             prefix[codeIndex] = (short) oldCode;
 *             suffix[codeIndex] = (byte) first;
 *             codeIndex++;
 *             if((0 == (codeIndex & codeMask)) && (codeSize < 12)){
 * //                System.out.println("index:"+index+" codeIndex:"+codeIndex+" codeSize:"+codeSize+" codeMask:"+codeMask);
 *                 codeSize++;
 *                 codeMask = (1 << codeSize) - 1;
 *             }
 * </pre>
 *
 * Refer to the Glide I fixed the problems.
 *
 * The first example is my implementation of decompress the bytes. Here we only get the byte buffer, not the finally pixels.
 * The second implementation copy from ImageMagick. Just use this implementation to test if we are corrected.
 */
public class GifDecodeTest {

    /**
     * Test GIF file decoding.
     *
     * @throws IOException
     */
    @Test
    public void decompressionTest() throws IOException {
        final File file=new File("../app/src/main/assets/image/image3.gif");
        GifHeaderDecoder decoder=new GifHeaderDecoder();
        decoder.loadFile(file);
        int frameCount = decoder.getFrameCount();
        int width = decoder.getWidth();
        int height = decoder.getHeight();
        System.out.println("File:"+file.getName()+" frameCount:"+frameCount);
        BufferedChannelReader reader = decoder.getBufferedReader();
        for(int i=0;i<frameCount;i++){
            GifHeaderDecoder.GifFrame frame = decoder.getFrame(i);
            byte[] bytes = getFrameByteArray(reader,frame);
            //My implementation.
            byte[] bytes1 = decompress1(bytes, width, height);
            //Code from Glide which is adapted from John Cristy's ImageMagick.
            byte[] bytes2 = decompress2(bytes, width, height);
            //Check if the byte arrays are equal to each other.
            for(int i1=0;i1<bytes1.length;i1++){
                assert bytes1[i1] == bytes2[i1];
            }
        }
        System.out.println("Decode corrected.");
    }


    public byte[] getFrameByteArray(BufferedChannelReader reader, GifHeaderDecoder.GifFrame frame) throws IOException {
        GifHeaderDecoder.GifDataBlock imageDescriptor = frame.imageDescriptor;
        long start = reader.position();
        ByteBuffer byteBuffer=ByteBuffer.allocate((int) (imageDescriptor.end-imageDescriptor.start));
        FileChannel fileChannel = reader.getFileChannel();
        fileChannel.position(start);
        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        return byteBuffer.array();
    }

    private void outputArray(byte[] arr,int rowCount,int columnCount){
        for(int row=0;row<rowCount;row++){
            for(int column=0;column<columnCount;column++){
                int i = row * columnCount + column;
                System.out.print((arr[i] & 0xFF)+",");
            }
            System.out.println();
        }
    }

    /**
     * My own implementation about how to decode the GIF image block.
     * @param codeStream
     * @param iw
     * @param ih
     * @return
     */
    private byte[] decompress1(byte[] codeStream, int iw, int ih) {
        int npix = iw * ih;
        final int MaxStackSize = 4096;
        byte[] pixels = new byte[npix]; // allocate new pixel array

        short[] prefix = new short[MaxStackSize];
        byte[] suffix = new byte[MaxStackSize];
        byte[] pixelStack = new byte[MaxStackSize + 1];
        int index=0;
        int minLZWSize=codeStream[index++];
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
        int blockSize=codeStream[index++] & 0xFF;
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
                pixels[pixelIndex++] = pixelStack[--stackIndex];
            }
            //Simplify the bit buffer. This is brilliant.
            if(bits < codeSize){
                if(0 == blockSize){
                    blockSize = codeStream[index++] & 0xFF;
                    if(0 >= blockSize){
                        break;
                    }
                }
                datum |= (codeStream[index++] & 0xFF) << bits;
                bits += 8;
                blockSize --;
                //If the bits less then the code size. keep working.
                continue;
            }
            code  = datum & codeMask;
            datum >>= codeSize;
            bits -= codeSize;
            if(code == eof){
                break;
            } else if(code == clearCode){
                codeSize=minLZWSize+1;
                codeMask=(1 << codeSize) - 1;
                codeIndex = eof+1;
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
            while(code > clearCode){
                pixelStack[stackIndex++] = suffix[code];
                code =  prefix[code];
            }
            //Bugs. Refer to the Glide.
            if (codeIndex >= MaxStackSize)
                break;
            first = suffix[code] & 0xff;
            pixelStack[stackIndex++] = (byte) first;
            prefix[codeIndex] = (short) oldCode;
            suffix[codeIndex] = (byte) first;
            codeIndex++;
            if((0 == (codeIndex & codeMask)) && (codeSize < 12)){
                codeSize++;
                codeMask = (1 << codeSize) - 1;
            }
            oldCode = curCode;
        }
        return pixels;
    }

    /**
     * @param codeStream
     * @return
     *
     * Code from Glide which is adapted from John Cristy's ImageMagick.
     */
    private byte[] decompress2(byte[] codeStream, int iw, int ih) {
        int index=0;
        int NullCode = -1;
        int npix = iw * ih;
        int codeIndex,
                clear,
                code_mask,
                code_size,
                end_of_information,
                in_code,
                old_code,
                bits,
                code,
                count,
                i,
                datum,
                data_size,
                first,
                top,
                bi,
                pi;

        final int MaxStackSize = 4096;
        byte[] pixels = new byte[npix]; // allocate new pixel array

        short[] prefix = new short[MaxStackSize];
        byte[] suffix = new byte[MaxStackSize];
        byte[] pixelStack = new byte[MaxStackSize + 1];

        //  Initialize GIF data stream decoder.
        data_size = codeStream[index++] & 0xFF;
        clear = 1 << data_size;
        end_of_information = clear + 1;
        codeIndex = clear + 2;
        old_code = NullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; code++) {
            prefix[code] = 0;
            suffix[code] = (byte) code;
        }
        //  Decode GIF pixel stream.
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix;) {
            if (top == 0) {
                if (bits < code_size) {
                    //  Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = codeStream[index++] & 0xFF;
                        if (count <= 0)
                            break;
                        bi = 0;
                    }
                    byte b = codeStream[index++];
                    datum += (b & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                //  Get the next code.
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;

                //  Interpret the code

                if ((code > codeIndex) || (code == end_of_information))
                    break;
                if (code == clear) {
                    //  Reset decoder.
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    codeIndex = clear + 2;
                    old_code = NullCode;
                    continue;
                }
                if (old_code == NullCode) {
                    pixelStack[top++] = suffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                in_code = code;
                if (code == codeIndex) {
                    pixelStack[top++] = (byte) first;
                    code = old_code;
                }
                while (code > clear) {
                    pixelStack[top++] = suffix[code];
                    code = prefix[code];
                }
                first = ((int) suffix[code]) & 0xff;

                //  Add a new string to the string table,

                if (codeIndex >= MaxStackSize)
                    break;
                pixelStack[top++] = (byte) first;
                prefix[codeIndex] = (short) old_code;
                suffix[codeIndex] = (byte) first;
                codeIndex++;
                if (((codeIndex & code_mask) == 0)
                        && (codeIndex < MaxStackSize)) {
//                    System.out.println("index:"+index+" codeIndex:"+codeIndex+" codeSize:"+code_size+" codeMask:"+code_mask);
                    code_size++;
                    code_mask += codeIndex;
                }
                old_code = in_code;
            }
//            System.out.println("top:"+top);
            //  Pop a pixel off the pixel stack.
            top--;
            pixels[pi++] = pixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++) {
            pixels[i] = 0; // clear missing pixels
        }
        return pixels;
    }


}
