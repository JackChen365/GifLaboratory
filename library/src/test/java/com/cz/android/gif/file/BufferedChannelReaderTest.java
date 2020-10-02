package com.cz.android.gif.file;

import com.cz.android.gif.reader.BufferedChannelReader;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Created by cz
 * @date 2020/9/16 3:27 PM
 * @email bingo110@126.com
 *
 * Buffered file change.
 * Because FileChannel actually do not have a buffer. And it sometimes make things difficult to deal with.
 * You have to take care of the ByteBuffer and take data from the buffer at the same time.
 *
 * So here we have the BufferedChannelReader. it helps you to take care of the buffer.
 * I've tested when the buffer size is only one. It works pretty good.
 */
public class BufferedChannelReaderTest {

    /**
     * Load a file and check if our buffer reader works fine.
     * @throws IOException
     */
    @Test
    public void testReadFile() throws IOException {
        File file = new File("../app/src/main/assets/image/image2.gif");
        byte[] bytes1 = readByteArray1(file);
        byte[] bytes2 = readByteArray2(file);
        //Check if two byte array are the same.
        for(int i=0;i<bytes1.length;i++){
            if(bytes1[i]!=bytes2[i]){
                throw new RuntimeException("The two array are now equal to each other!");
            }
        }
        System.out.println("The two byte arrays are equal to each other.");
    }

    /**
     * Test read primitive type use our buffered reader.
     * @throws IOException
     */
    @Test
    public void testReadPrimitive() throws IOException {
        File file = new File("../app/src/main/assets/image/image2.gif");
        byte[] bytes = readByteArray1(file);

        FileInputStream inputStream = new FileInputStream(file);
        BufferedChannelReader bufferedReader = new BufferedChannelReader(inputStream.getChannel());

        short v1 = bufferedReader.readShort();
        int v2 = bufferedReader.readInt();

        for(int i=0;i<6;i++){
            System.out.println(padStart(Integer.toBinaryString(bytes[i]),8,'0'));
        }
        System.out.println(padStart(Integer.toBinaryString(v1),16,'0'));
        System.out.println(padStart(Integer.toBinaryString(v2),32,'0'));
    }

    /**
     * Read all the bytes from the given file.
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] readByteArray1(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        int available = inputStream.available();
        byte[] bytes=new byte[available];
        int b;
        int index=0;
        while(-1!=(b=inputStream.read())){
            bytes[index++]= (byte) b;
        }
        inputStream.close();
        return bytes;
    }

    /**
     * Read all the bytes by using the {@link BufferedChannelReader} from the given file.
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] readByteArray2(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        BufferedChannelReader bufferedReader = new BufferedChannelReader(inputStream.getChannel());
        int available = inputStream.available();
        byte[] bytes=new byte[available];
        int index=0;
        //Here we can not use bufferedReader.readByte()
        //The byte sometimes equals to -1. it will make the process of reading terminate earlier.
        int b=bufferedReader.readByteUnsigned();
        while(-1 != b){
            bytes[index++]= (byte) b;
            b=bufferedReader.readByteUnsigned();
        }
        bufferedReader.close();
        return bytes;
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
}
