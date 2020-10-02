#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include "gif/GifDecoder.h"

#define TAG "NativeGifDecoder"
#define  logI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define  logE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
//
// Created by Jack Chen on 9/26/2020.
//
extern "C"
JNIEXPORT jlong JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeLoadFile(JNIEnv *env, jobject thiz,
                                                                 jstring file_file) {
    char* path = (char *) env->GetStringUTFChars(file_file, 0);
    GifDecoder *decoder = new GifDecoder();
    if (decoder->loadImage(path)) {
        logI("nativeLoadFile:%s success.",path);
        env->ReleaseStringUTFChars(file_file,path);
        return (jlong)decoder;
    }
    logI("nativeLoadFile:%s failed.",path);
    free(decoder);
    env->ReleaseStringUTFChars(file_file,path);
    return -1;
}

extern "C"
JNIEXPORT jshort JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeGetDelay(JNIEnv *env, jobject thiz,
                                                                 jlong ref,jint index) {
    GifDecoder* decoder=(GifDecoder*)ref;
    return decoder->getDelayTime(index);
}

extern "C"
JNIEXPORT jshort JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeGetWidth(JNIEnv *env, jobject thiz,
                                                                 jlong ref) {
    GifDecoder* decoder=(GifDecoder*)ref;
    return decoder->getWidth();
}

extern "C"
JNIEXPORT jshort JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeGetHeight(JNIEnv *env, jobject thiz,
                                                                  jlong ref) {
    GifDecoder* decoder=(GifDecoder*)ref;
    return decoder->getHeight();
}

extern "C"
JNIEXPORT jshort JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeGetFrameCount(JNIEnv *env, jobject thiz,
                                                                      jlong ref) {
    GifDecoder* decoder=(GifDecoder*)ref;
    return decoder->getFrameSize();
}

extern "C"
JNIEXPORT jshort JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeGetLoopCount(JNIEnv *env, jobject thiz,
                                                                     jlong ref) {
    GifDecoder* decoder=(GifDecoder*)ref;
    return decoder->getLoopCount();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeFillFrame(JNIEnv *env, jobject thiz,
                                                                      jlong ref,jint index, jobject bitmap) {

    GifDecoder* decoder=(GifDecoder*)ref;
    uint16_t& width = decoder->getWidth();
    uint16_t& height = decoder->getHeight();
    uint16_t frameIndex=(uint16_t)index;
    uint32_t* pixels=decoder->decodeFrame(frameIndex);
    void* bitmapPixels;

    AndroidBitmapInfo  info;
    int ret;
    if(ANDROID_BITMAP_RESULT_SUCCESS!=(ret=AndroidBitmap_getInfo(env,bitmap,&info))){
        return ret;
    }
    //If this succeeds, *addrPtr will be set to the pixel address. If the call fails, addrPtr will be ignored.
//    AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels);
    if (ANDROID_BITMAP_RESULT_SUCCESS!=(ret=AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels))){
        return ret;
    }
    uint32_t* dst=(uint32_t*)bitmapPixels;
    memcpy(dst, pixels, info.stride*height);
    AndroidBitmap_unlockPixels(env, bitmap);
    return ANDROID_BITMAP_RESULT_SUCCESS;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeReadFrame(JNIEnv *env, jobject thiz,
                                                                  jlong ref, jint index) {
    GifDecoder* decoder=(GifDecoder*)ref;
    uint16_t& width = decoder->getWidth();
    uint16_t& height = decoder->getHeight();
    uint16_t frameIndex=(uint16_t)index;
    uint32_t* pixels=decoder->decodeFrame(frameIndex);

    jintArray arr=env->NewIntArray(width*height);
    jint* arr1=env->GetIntArrayElements(arr,NULL);
    memcpy(arr1,pixels,width*height);
    env->ReleaseIntArrayElements(arr,arr1,0);
    return arr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cz_android_gif_sample_ndk_NativeDecoder_nativeFree(JNIEnv *env, jobject thiz,
                                                              jlong ref) {
    logI("nativeFree:%d success.",ref);
    GifDecoder* decoder=(GifDecoder*)ref;
    delete(decoder);
}