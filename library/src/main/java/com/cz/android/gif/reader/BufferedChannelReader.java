package com.cz.android.gif.reader;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A buffered file channel reader.
 * Used for read primitive data from file channel more easily.
 *
 * @see #readBoolean()
 * @see #readByte()
 * @see #readByteUnsigned()
 * @see #readShort()
 * @see #readShortLe()
 * @see #readInt()
 * @see #readInt(int)
 * @see #readIntUnsigned(int)
 * @see #readFloat()
 * @see #readLong()
 * @see #readLong(int)
 * @see #readLongUnsigned(int)
 * @see #readDouble()
 * @see #readString(int)
 * @see #readString(int, Charset)
 * @see #readByteBuffer(ByteBuffer)
 */
public class BufferedChannelReader implements Reader,Closeable {
    private static int defaultCharBufferSize = 8*1024;
    private ByteBuffer byteBuffer;
    private FileChannel fileChannel;

    public BufferedChannelReader(FileChannel fileChannel) {
        this(fileChannel,defaultCharBufferSize);
    }

    public BufferedChannelReader(FileChannel fileChannel, int bufferSize) {
        this.fileChannel = fileChannel;
        this.byteBuffer=ByteBuffer.allocate(bufferSize);
        this.byteBuffer.position(byteBuffer.limit());
    }

    /**
     * Checks to make sure that the stream has not been closed
     **/
    private void ensureChannel() throws IOException {
        if (fileChannel == null)
            throw new IOException("Stream closed");
        if(!byteBuffer.hasRemaining()){
            byteBuffer.clear();
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
        }
    }

    /**
     * @return 32 bit signed integer value
     */
    @Override
    public short readShortLe() throws IOException {
        return (short) (readByte() & 0xFF | ((readByte() & 0xFF) << 8));
    }

    /**
     * @return 32 bit signed integer value
     */
    @Override
    public short readShort() throws IOException {
        return (short) (((readByte()&0xFF) << 8) | (readByte()&0xFF));
    }

    /**
     * @return 32 bit signed integer value
     */
    @Override
    public int readInt() throws IOException {
        return ((readByte()&0xFF) << 24) | ((readByte()&0xFF) << 16) | ((readByte()&0xFF) << 8) | (readByte()&0xFF);
    }

    /**
     * @param bits Length of integer
     * @return Signed integer value of given bit width
     */
    @Override
    public int readInt(int bits) throws IOException {
        if(bits == 0)return 0;
        boolean sign = readBoolean();
        int inBits = --bits;

        int res = 0;
        do {
            if(bits > 7){
                res = (res << 8) | (readByte()&0xFF);
                bits -= 8;
            }else{
                res = (res << bits) + (readByte());
                bits -= bits;
            }
        }while(bits > 0);

        return sign ? (0xFFFFFFFF << inBits) | res : res;
    }

    @Override
    public boolean readBoolean() throws IOException {
        byte b = peekByte();
        return 0 < (b & 0x80);
    }

    /**
     * @param bits Length of integer
     * @return Unsigned Integer value of given bit width
     */
    @Override
    public int readIntUnsigned(int bits) throws IOException {
        if(bits == 0)return 0;
        int res = 0;
        do {
            if(bits > 7){
                res = (res << 8) | (readByte()&0xFF);
                bits -= 8;
            }else{
                res = (res << bits) + (readByteUnsigned());
                bits -= bits;
            }
        }while(bits > 0);
        return res;
    }

    /**
     * @return 64 bit signed long value
     */
    @Override
    public long readLong() throws IOException {
        return ((readByte()&0xFFL) << 56L) | ((readByte()&0xFFL) << 48L) | ((readByte()&0xFFL) << 40L) | ((readByte()&0xFFL) << 32L)
                | ((readByte()&0xFFL) << 24L) | ((readByte()&0xFFL) << 16L) | ((readByte()&0xFFL) << 8L) | (readByte()&0xFFL);
    }

    /**
     * @param bits Length of long integer
     * @return Signed long value of given bit width
     */
    @Override
    public long readLong(int bits) throws IOException {
        if(bits == 0)return 0;
        boolean sign = readBoolean();
        int inBits = --bits;

        long res = 0;
        do {
            if(bits > 31){
                res = (res << 32L) | (readInt()&0xFFFFFFFFL);
                bits -= 32;
            }else{
                res = (res << bits) | (readIntUnsigned(bits)&0xFFFFFFFFL);
                bits -= bits;
            }
        }while(bits > 0);
        return (sign ? (0xFFFFFFFFFFFFFFFFL << (long) inBits) | res : res);
    }

    /**
     * @param bits Length of long integer
     * @return Unsigned long value of given bit width
     */
    @Override
    public long readLongUnsigned(int bits) throws IOException {
        if(bits == 0)return 0;
        long res = 0;
        do {
            if(bits > 31){
                res = (res << 32L) | (readInt()&0xFFFFFFFFL);
                bits -= 32;
            }else{
                res = (res << bits) | (readIntUnsigned(bits)&0xFFFFFFFFL);
                bits -= bits;
            }
        }while(bits > 0);
        return res;
    }

    /**
     * @return 32 bit floating point value
     */
    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * @return 64 bit floating point value
     */
    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * @param length Length of the string
     * @return String of given length, using ASCII encoding
     */
    @Override
    public String readString(int length) throws IOException {
        byte[] bytes = new byte[length];
        for(int i = 0; i < length; ++i){
            bytes[i] = readByte();
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * @param length Length of the string
     * @param charset {@link Charset} to use for decoding
     * @return String of given length, using ASCII encoding
     */
    @Override
    public String readString(int length, Charset charset) throws IOException {
        byte[] bytes = new byte[length];
        for(int i = 0; i < length; ++i){
            bytes[i] = readByte();
        }
        return new String(bytes, charset);
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    private int readBuffer()throws IOException{
        byteBuffer.clear();
        int read = fileChannel.read(byteBuffer);
        byteBuffer.flip();
        return read;
    }

    public ByteBuffer getBuffer(){
        return byteBuffer;
    }

    @Override
    public int readByteUnsigned() throws IOException {
        ensureChannel();
        if(byteBuffer.position()>=byteBuffer.limit()){
            return -1;
        } else {
            return byteBuffer.get() & 0xFF;
        }
    }

    @Override
    public byte peekByte() throws IOException {
        ensureChannel();
        int position = byteBuffer.position();
        byte data = byteBuffer.get();
        byteBuffer.position(position);
        return data;
    }

    @Override
    public byte readByte() throws IOException {
        ensureChannel();
        return byteBuffer.get();
    }

    public boolean hasRemaining() throws IOException {
        ensureChannel();
        return byteBuffer.position()>=byteBuffer.limit();
    }

    @Override
    public long position() throws IOException {
        long position = fileChannel.position();
        return position-byteBuffer.remaining();
    }

    public void position(long newPosition) throws IOException {
        fileChannel.position(newPosition);
        readBuffer();
    }

    @Override
    public long size() throws IOException {
        return fileChannel.size();
    }

    /**
     * Read file channel by a internal buffer
     * @return
     * @throws IOException
     */
    @Override
    public String readLine() throws IOException {
        ensureChannel();
        String line;
        String remainString=null;
        //First time when we readByte the line.
        if(!byteBuffer.hasRemaining()){
            byteBuffer.clear();
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
        }
        while(byteBuffer.hasRemaining()&&null!=(line=readByteBuffer(byteBuffer))){
            if ('\n' == line.charAt(line.length() - 1)) {
                //End with line feeds, just return the line.
                if(null!=remainString){
                    return remainString+line;
                } else {
                    return line;
                }
            }
            if(!byteBuffer.hasRemaining()){
                if(null!=remainString){
                    remainString += line;
                } else {
                    remainString = line;
                }
                byteBuffer.clear();
                //Nothing to readByte.
                if(0 > fileChannel.read(byteBuffer)){
                    byteBuffer.position(byteBuffer.limit());
                    if(null!=remainString){
                        return remainString;
                    } else {
                        return line;
                    }
                }
                byteBuffer.flip();
            }
        }
        return null;
    }

    private String readByteBuffer(ByteBuffer byteBuffer) throws EOFException {
        StringBuilder result = new StringBuilder(80);
        while (byteBuffer.hasRemaining()) {
            char c = (char) byteBuffer.get();
            result.append(c);
            if (c == -1) {
                throw new EOFException();
            } else if (c == '\n') {
                break;
            }
        }
        return result.toString();
    }

    @Override
    public void skip(int num) throws IOException {
        if(num < byteBuffer.remaining()){
            int position = byteBuffer.position();
            byteBuffer.position(position+num);
        } else {
            num-=byteBuffer.remaining();
            readBuffer();
            skip(num);
        }
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
    }
}
