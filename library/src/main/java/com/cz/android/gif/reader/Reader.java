package com.cz.android.gif.reader;

import java.io.IOException;
import java.nio.charset.Charset;

public interface Reader {
    /**
     * @return 32 bit signed integer value
     */
    short readShortLe() throws IOException;

    /**
     * @return 32 bit signed integer value
     */
    short readShort() throws IOException;

    /**
     * @return 32 bit signed integer value
     */
    int readInt() throws IOException;

    /**
     * @param bits Length of integer
     * @return Signed integer value of given bit width
     */
    int readInt(int bits) throws IOException;

    boolean readBoolean() throws IOException;

    /**
     * @param bits Length of integer
     * @return Unsigned Integer value of given bit width
     */
    int readIntUnsigned(int bits) throws IOException;

    /**
     * @return 64 bit signed long value
     */
    long readLong() throws IOException;

    /**
     * @param bits Length of long integer
     * @return Signed long value of given bit width
     */
    long readLong(int bits) throws IOException;

    /**
     * @param bits Length of long integer
     * @return Unsigned long value of given bit width
     */
    long readLongUnsigned(int bits) throws IOException;

    /**
     * @return 32 bit floating point value
     */
    float readFloat() throws IOException;

    /**
     * @return 64 bit floating point value
     */
    double readDouble() throws IOException;

    /**
     * @param length Length of the string
     * @return String of given length, using ASCII encoding
     */
    String readString(int length) throws IOException;

    /**
     * @param length Length of the string
     * @param charset {@link Charset} to use for decoding
     * @return String of given length, using ASCII encoding
     */
    String readString(int length, Charset charset) throws IOException;

    int readByteUnsigned() throws IOException;

    byte peekByte() throws IOException;

    byte readByte() throws IOException;

    boolean hasRemaining() throws IOException;

    long position() throws IOException;

    long size() throws IOException;

    /**
     * Read file channel by a internal buffer
     * @return
     * @throws IOException
     */
    String readLine() throws IOException;

    void skip(int num) throws IOException;

    void close() throws IOException;
}
