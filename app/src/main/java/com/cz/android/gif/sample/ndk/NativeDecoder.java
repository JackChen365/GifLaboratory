package com.cz.android.gif.sample.ndk;

import android.graphics.Bitmap;

/**
 * @author Created by cz
 * @date 2020/9/30 4:15 PM
 * @email bingo110@126.com
 *
 * The native decoder, used for The view to display a gif image.
 * You could easily use this decoder to implement a GifDisplayView.
 *
 * Please refer to the cpp file:native-gif-lib.cpp
 *
 * @see NativeGifView
 * @see NativeTextureGifView
 */
public class NativeDecoder {
    private static final int INVALID_OBJECT_REF=-1;

    public static final int ANDROID_BITMAP_RESULT_SUCCESS           = 0;
    /**
     * Bad parameter.
     */
    public static final int ANDROID_BITMAP_RESULT_BAD_PARAMETER     = -1;
    /**
     * JNI exception occured.
     */
    public static final int ANDROID_BITMAP_RESULT_JNI_EXCEPTION     = -2;
    /**
     * Allocation failed.
     */
    public static final int ANDROID_BITMAP_RESULT_ALLOCATION_FAILED = -3;

    private native long nativeLoadFile(String fileFile);
    private native short nativeGetWidth(long ref);
    private native short nativeGetHeight(long ref);
    private native short nativeGetFrameCount(long ref);
    private native int nativeFillFrame(long ref, int index, Bitmap bitmap);
    private native short nativeGetDelay(long ref,int index);
    private native short nativeGetLoopCount(long ref);
    private native void nativeFree(long ref);

    /**
     * The native decoder pointer.
     * It should be a  long type. if it in x64 operating system.
     */
    private long decoderPointer =INVALID_OBJECT_REF;

    static {
        System.loadLibrary("gif-native-lib");
    }

    public boolean isReady(){
        return INVALID_OBJECT_REF != decoderPointer;
    }

    public boolean isRecycled(){
        return INVALID_OBJECT_REF == decoderPointer;
    }

    public long getDecoderPointer() {
        return decoderPointer;
    }

    /**
     * Load a Gif file.
     * @param fileFile
     * @return
     */
    public boolean loadFile(String fileFile){
        decoderPointer = nativeLoadFile(fileFile);
        return INVALID_OBJECT_REF != decoderPointer;
    }

    /**
     * Return the image logical width.
     * @return
     */
    public short getWidth(){
        if(INVALID_OBJECT_REF==decoderPointer){
            throw new RuntimeException("The pointer is invalid! Please make sure you call the method: loadFile!");
        }
        return nativeGetWidth(decoderPointer);
    }

    /**
     * Return the image logical height.
     * @return
     */
    public short getHeight(){
        if(INVALID_OBJECT_REF==decoderPointer){
            throw new RuntimeException("The pointer is invalid! Please make sure you call the method: loadFile!");
        }
        return nativeGetHeight(decoderPointer);
    }

    /**
     * The total frame size.
     * @return
     */
    public short getFrameCount(){
        if(INVALID_OBJECT_REF==decoderPointer){
            throw new RuntimeException("The pointer is invalid! Please make sure you call the method: loadFile!");
        }
        return nativeGetFrameCount(decoderPointer);
    }

    /**
     * Fill the image.
     * @param index
     * @param bitmap
     * @return
     */
    public int fillFrame(int index, Bitmap bitmap){
        if(INVALID_OBJECT_REF!=decoderPointer){
            return nativeFillFrame(decoderPointer,index,bitmap);
        } else {
            return ANDROID_BITMAP_RESULT_ALLOCATION_FAILED;
        }
    }

    /**
     * Return the delay time.
     * @param index
     * @return
     */
    public short getDelay(int index){
        if(INVALID_OBJECT_REF==decoderPointer){
            throw new RuntimeException("The pointer is invalid! Please make sure you call the method: loadFile!");
        }
        return nativeGetDelay(decoderPointer,index);
    }

    /**
     * The loop count. If it equal to zero. It means loop infinite.
     * @return
     */
    public short getLoopCount(){
        if(INVALID_OBJECT_REF==decoderPointer){
            throw new RuntimeException("The pointer is invalid! Please make sure you call the method: loadFile!");
        }
        return nativeGetLoopCount(decoderPointer);
    }

    /**
     * Free the memory in native.
     */
    public void free(){
        if(INVALID_OBJECT_REF!=decoderPointer){
            nativeFree(decoderPointer);
            decoderPointer = INVALID_OBJECT_REF;
        }
    }
}
