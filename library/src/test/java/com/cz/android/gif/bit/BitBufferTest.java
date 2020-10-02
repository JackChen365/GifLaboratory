package com.cz.android.gif.bit;

/**
 * Test the bit buffer.
 * Because For a GIF file. We are not going to put all the byte code into the file. Instead we use a code size to save the bit.
 * If the code size was three. We only put three bits to the buffer, and when you decode the byte.
 * You have to always remember that if the code side is three. One code only take three bits.
 *
 * Please refer to the offline document:./references/What's In A GIF - LZW Image Data.html
 *
 * @see BitBuffer
 */
public class BitBufferTest {
    public static void partialByteTest() {
        int[] arr= new int[]{5,7,15,31,33,35,127};
        int[] bits= new int[]{4,4,6,6,7,7,8};
        for(int v:arr){
            System.out.println(padStart(Integer.toBinaryString(v),8,'0')+" "+v);
        }
//        00000101 5
//        00000111 7
//        00001111 15
//        00011111 31
//        00100001 33
//        00100011 35
//        01111111 127
//        01110101
        BitBuffer buffer = BitBuffer.allocate(1024*8);
        for(int i=0;i<arr.length;i++){
            buffer.putByte((byte) arr[i],bits[i]);
        }
        buffer.flip();
        while(buffer.hasRemaining()){
            byte b = buffer.readByte();
            System.out.println(padStart(Integer.toBinaryString(b & 0xFF),8,'0'));
        }
    }

    public static CharSequence padStart(CharSequence text,int length,char padChar){
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
}
