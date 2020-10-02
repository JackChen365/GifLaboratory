#include <jni.h>
#include <string>
#include <unistd.h>
#include <vector>
#include <android/log.h>
#include <android/bitmap.h>
#include <jni.h>

#define TAG "NativeBasic"

#define  logI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define  logE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

//--------------------------------------------------------------
//The native method list are for HelloNativeSampleActivity
//--------------------------------------------------------------
extern "C"
JNIEXPORT void JNICALL Java_com_cz_android_gif_sample_ui_test_BitmapFillTestActivity_fillBitmap(JNIEnv* env,jobject thiz, jobject bitmap,jint width, jint height,jint color) {

    AndroidBitmapInfo info;
    if(ANDROID_BITMAP_RESULT_SUCCESS!=AndroidBitmap_getInfo(env,bitmap,&info)){
        throw "Invoke Bitmap AndroidBitmap_getInfo failed.";
    }
    void* bitmapPixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels) < 0) {
        throw "Invoke Bitmap AndroidBitmap_lockPixels method failed.";
    }
    uint32_t* dst=(uint32_t*)bitmapPixels;
    std::fill(dst,dst+width*height,color);
    AndroidBitmap_unlockPixels(env, bitmap);
}
