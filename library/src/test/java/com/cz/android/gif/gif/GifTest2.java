package com.cz.android.gif.gif;


import com.cz.android.gif.bit.BitBuffer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a complete sample for how to encode and decode GIF image data.
 * Unlike the {@link GifTest1} only shows you how to encode and decode part of the data.
 * This example totally use the basic pixel data encode and decode.
 */
public class GifTest2 {

    @Test
    public void compressionTest() {
        //10*10
        int[] colorTable=new int[4];
        colorTable[0]=-1;
        colorTable[1]=-65536;
        colorTable[2]=-16776961;
        colorTable[3]=-16777216;
        //pixel
        int[] pixelArray=new int[]{
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,0,0,0,0,2,2,2,
                1,1,1,0,0,0,0,2,2,2,
                2,2,2,0,0,0,0,1,1,1,
                2,2,2,0,0,0,0,1,1,1,
                2,2,2,2,2,1,1,1,1,1,
                2,2,2,2,2,1,1,1,1,1,
                2,2,2,2,2,1,1,1,1,1
        };
        compressTest(colorTable,pixelArray);
        decompressTest(colorTable);
    }

    /**
     * The first example is to demonstrate how to reduce the bit use minimum LZW size.
     * See if how we generate the same byte array.
     */
    private void compressTest(int[] colorTable, int[] pixelArray) {
        //02	16	8C	2D	99	87	2A	1C	DC	33	A0	02	75	EC	95	FA	A8	DE	60	8C	04	91	4C	01
        int[] byteArray=new int[]{0x02,0x16,0x8C,0x2D,0x99,0x87,0x2A,0x1C,0xDC,0x33,0xA0,0x02,0x75,0xEC,0x95,0xFA,0xA8,0xDE,0x60,0x8C,0x04,0x91,0x4C,0x01};
        for(int v:byteArray){
            byte b = (byte) v;
            System.out.print(padStart(Integer.toBinaryString(b&0xFF),8,'0')+",");
        }
        System.out.println();
        int minLZWSize = 2;
        BitBuffer bitBuffer = compress(colorTable, pixelArray,minLZWSize);
        bitBuffer.flip();
        byte[] remainByteArray = bitBuffer.getRemainByteArray();
        byte[] encodeByteArray=new byte[remainByteArray.length+2];
        encodeByteArray[0]= (byte) minLZWSize;
        encodeByteArray[1]= (byte) remainByteArray.length;
        System.arraycopy(remainByteArray,0,encodeByteArray,2,remainByteArray.length);
        for(byte b:encodeByteArray){
            System.out.print(padStart(Integer.toBinaryString(b&0xFF),8,'0')+",");
        }
        System.out.println();
    }

    /**
     * Compress the image and generate the byte array.
     * @param colorTable
     * @param pixelArray
     * @param minLZWSize
     * @return
     */
    private BitBuffer compress(int[] colorTable, int[] pixelArray,int minLZWSize){
        //1.Initialize code table
        BitBuffer bitBuffer = BitBuffer.allocate(4096 * 8);
        Map<List<Integer>,Integer> codeTable=new HashMap<>();
        for(int i=0;i<colorTable.length;i++) {
            codeTable.put(Arrays.asList(i), i);
        }
        int codeSize=minLZWSize+1;
        int bitSize=(1 << codeSize);

        int colorCodes=(1 << minLZWSize) - 1;
        int clearCode = colorCodes+1;
        bitBuffer.putByte((byte) clearCode,codeSize);
        int eof = colorCodes+2;
        int codeIndex=eof+1;

        //2.Always start by sending a clear code to the code stream.
//        result.add(clearCode);
//        bitBuffer.put((byte)clearCode,codeSize);
        //3.Read first index from index stream. This value is now the value for the index buffer
        List<Integer> previous=new ArrayList<>();
        List<Integer> current;
        int index=0;
        int K = pixelArray[index++];
        previous.add(K);
        //<LOOP POINT>
        while(index < pixelArray.length){
            K=pixelArray[index++];
            current=new ArrayList<>(previous.size()+1);
            current.addAll(previous);
            current.add(K);
            if(codeTable.containsKey(current)){
                previous=current;
            } else {
                codeTable.put(current,codeIndex++);
                int code = codeTable.get(previous);
                bitBuffer.putByte((byte) code,codeSize);
                previous.clear();
                previous.add(K);
            }
            if(codeIndex > bitSize){
                codeSize++;
                if(codeSize > 12){
                    codeSize = 12;
                }
                bitSize = (1 << codeSize);
            }
        }
        if(!previous.isEmpty()){
            int code = codeTable.get(previous);
            bitBuffer.putByte((byte) code,codeSize);
        }
        bitBuffer.putByte((byte) eof,codeSize);
        return bitBuffer;
    }

    /**
     * Decode test.
     * We use this original byte array to decode the pixel array.
     * @param colorTable
     */
    private void decompressTest(int[] colorTable) {
        int[] arr=new int[]{0x02,0x16,0x8C,0x2D,0x99,0x87,0x2A,0x1C,0xDC,0x33,0xA0,0x02,0x75,0xEC,0x95,0xFA,0xA8,0xDE,0x60,0x8C,0x04,0x91,0x4C,0x01};
        byte[] codeStream=new byte[arr.length];
        for(int i=0;i<arr.length;i++){
            codeStream[i]= (byte) arr[i];
        }
        List<Integer> decompress = decompress(colorTable, codeStream);
        outputList(decompress,10,10);
    }

    private void outputList(List<Integer> arr,int rowCount,int columnCount){
        for(int row=0;row<rowCount;row++){
            for(int column=0;column<columnCount;column++){
                int i = row * columnCount + column;
                System.out.print(arr.get(i)+",");
            }
            System.out.println();
        }
    }

    private List<Integer> decompress(int[] colorTable, byte[] codeStream) {
        BitBuffer bitBuffer = BitBuffer.wrap(codeStream);
        List<Integer> result=new ArrayList<>();
        int index=0;
        int clearCode=colorTable.length;
        int eof=clearCode+1;

        int minLZWSize=bitBuffer.readByte();
        int codeSize=minLZWSize+1;
        int bitSize=(1 << codeSize);
        Map<Integer,List<Integer>> codeTable=new HashMap<>();
        for(int i=0;i<colorTable.length;i++){
            codeTable.put(i,Arrays.asList(i));
        }
        int codeIndex = eof+1;
        //let CODE be the first code in the code stream
        //output {CODE} to index stream
        int blockSize=bitBuffer.readByte();
        int previousCode;
        if(clearCode != bitBuffer.readByte(codeSize)){
            //The bad image data.
        }
        int currentCode=bitBuffer.readByte(codeSize);
        List<Integer> list = codeTable.get(currentCode);
        output(result,list);
        List<Integer> previous;
        while(currentCode != eof){
            previousCode = currentCode;
            currentCode = bitBuffer.readByte(codeSize);
            if(currentCode == eof){
                break;
            } else if(currentCode == clearCode){
                codeSize=minLZWSize+1;
                bitSize=(1 << codeSize);
                codeTable.clear();
                for(int i=0;i<colorTable.length;i++){
                    codeTable.put(i,Arrays.asList(i));
                }
            }
            if(codeTable.containsKey(currentCode)){
                List<Integer> value=codeTable.get(currentCode);
                System.out.println("currentCode:"+currentCode+" list:"+value);
                output(result,value);
                int c = value.get(0);

                previous = codeTable.get(previousCode);
                list=new ArrayList<>();
                list.addAll(previous);
                list.add(c);
                codeTable.put(codeIndex++,list);
            } else {
                previous = codeTable.get(previousCode);
                int c = previous.get(0);
                list=new ArrayList<>();
                list.addAll(previous);
                list.add(c);
                codeTable.put(codeIndex++,list);

                System.out.println("currentCode:"+currentCode+" list:"+list);
                output(result,list);
            }
            if(codeIndex == bitSize){
                codeSize++;
                if(codeSize > 12){
                    codeSize = 12;
                }
                bitSize = (1 << codeSize);
            }
        }
        return result;
    }

    public CharSequence padStart(CharSequence text,int length,char padChar){
        if (length < 0)
            throw new IllegalArgumentException("Desired length $length is less than zero.");
        if (length <= text.length())
            return text.subSequence(0, text.length());

        StringBuilder sb = new StringBuilder(length);
        for (int i=1;i<=(length - text.length());i++)
            sb.append(padChar);
        sb.append(text);
        return sb;
    }

    private void output(List<Integer> result,List<Integer> list){
        for(Integer i:list){
            result.add(i);
        }
    }
}
