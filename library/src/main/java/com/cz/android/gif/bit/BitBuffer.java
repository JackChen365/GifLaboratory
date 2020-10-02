package com.cz.android.gif.bit;

/**
 * The bit buffer. Used to save a few bits or take bits from the buffer.
 * This is helpful. if you only want to deal with the bit.
 * For instance:
 * //        00000101 5
 * //        00000111 7
 * //        00001111 15
 * //        00011111 31
 *
 * if you save the number five into the buffer. for some reason you want to save the bit. and only put four bits.
 * you invoke the method: {@link #putByte(byte, int)}
 * then you know exactly how many bit you put and how many you want to take.
 * For two number five and seven you could save one byte.
 */
public class BitBuffer {
    private static final int BYTE_SIZE=8;
    public static BitBuffer allocate(int n) {
        return new BitBuffer(n);
    }

    /**
     * Wraps bitbuffer around given array instance.
     * Any operation on this bitBuffer will modify the array
     * @param array A byte array to wrap this buffer around
     * @return Newly created instance of BitBuffer wrapped around array
     */
    public static BitBuffer wrap(byte[] array){
        return new BitBuffer(array);
    }

    private final byte[] buffer;
    private int position;
    private int limit;

    private BitBuffer(int n) {
        this.buffer = new byte[n];
    }

    private BitBuffer(byte[] buffer) {
        this.buffer = buffer;
        this.position = 0;
        this.limit = buffer.length;
    }

    public void put(byte b){
        putByte(b,8);
    }

    public void putByte(byte b,int num){
        for(int i=0;i<num;i++){
            int bit =  (b >> i) & 1;
            int offset = position / BYTE_SIZE;
            buffer[offset] |= (byte) (bit << (position % BYTE_SIZE));
            position++;
            limit++;
        }
    }

    public void flip(){
        limit=position;
        position=0;
    }

    public byte readByte(){
        return readByte(BYTE_SIZE);
    }

    public byte readByte(int bits){
        byte r = 0;
        for(int i=0;i<bits;i++){
            int index = position / BYTE_SIZE;
            int bit = (buffer[index] >> position % BYTE_SIZE) & 1;
            r |= bit << i;
            position++;
        }
        return r;
    }

    public boolean hasRemaining(){
        return position < limit;
    }

    public byte[] getRemainByteArray(){
        int num = (0 == limit % 8) ? limit / 8 : limit / 8 + 1;
        byte[] bytes=new byte[num];
        System.arraycopy(buffer,0,bytes,0,num);
        return bytes;
    }

    public int position() {
        return position;
    }

    public int limit(){
        return limit;
    }
}
