package com.cz.android.gif.sample.ndk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

/**
 * The native decode implementation.
 * Please refer to the cpp file:native-gif-lib.cpp
 *
 * @see NativeDecoder help us manager the native decoder.
 *
 */
public class NativeGifView extends View {
    private static final String TAG="NativeGifView";
    /**
     * The native decoder.
     */
    private NativeDecoder nativeDecoder=new NativeDecoder();
    private Matrix matrix=new Matrix();
    private Bitmap currentFrame;
    private int frameIndex;
    private int loopIndex;
    private boolean isRunning;
    private float imageScale =1f;

    /**
     * The update action to update the frame.
     */
    private final Runnable updateAction=new Runnable() {
        @Override
        public void run() {
            if(nativeDecoder.isReady()){
                int frameCount = nativeDecoder.getFrameCount();
                if(frameIndex < frameCount){
                    long st = SystemClock.elapsedRealtime();
                    if(NativeDecoder.ANDROID_BITMAP_RESULT_SUCCESS==nativeDecoder.fillFrame(frameIndex,currentFrame)){
                        int delay = nativeDecoder.getDelay(frameIndex++);
                        Log.i(TAG,"id:"+hashCode()+" frameIndex:"+frameIndex+" time:"+(SystemClock.elapsedRealtime()-st)+" delay:"+delay);
                        postDelayed(this,delay);
                        invalidate();
                    }
                } else {
                    frameIndex = 0;
                    int loopCount = nativeDecoder.getLoopCount();
                    if(0 == loopCount){
                        post(this);
                    } else if(loopIndex < loopCount){
                        post(this);
                        loopIndex++;
                    }
                }
            }
        }
    };

    public NativeGifView(Context context) {
        super(context);
    }

    public NativeGifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NativeGifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Load the image from the given file.
     * @param imageFile
     */
    public void loadImage(File imageFile) {
        if(imageFile.exists()){
            if(nativeDecoder.loadFile(imageFile.getAbsolutePath())){
                int width = nativeDecoder.getWidth();
                int height = nativeDecoder.getHeight();
                currentFrame=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
                Bitmap.Config config = currentFrame.getConfig();
                requestLayout();
            }
        }
    }

    public void loadImage(String filePath) throws IOException {
        loadImage(new File(filePath));
    }

    /**
     * Determine the animation if running.
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Start the animation.
     */
    public void start(){
        if(!isRunning){
            isRunning=true;
            frameIndex=0;
            loopIndex =0;
            int delayTime = nativeDecoder.getDelay(frameIndex);
            postDelayed(updateAction,delayTime);
        }
    }

    /**
     * Resume the animation if the animation if pause.
     */
    public void resume(){
        isRunning=true;
        post(updateAction);
    }

    /**
     * Pause the animation. It usually cooperate with the method {@link #resume()}
     */
    public void pause(){
        isRunning=false;
        removeCallbacks(updateAction);
    }

    /**
     * Stop the animation.
     */
    public void stop(){
        isRunning=false;
        frameIndex=0;
        removeCallbacks(updateAction);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        if(nativeDecoder.isReady()){
            int measuredWidth = getMeasuredWidth();
            int width = nativeDecoder.getWidth();
            int height = nativeDecoder.getHeight();
            if(0 == width || 0 == height){
                setMeasuredDimension(0,0);
            } else {
                imageScale = Math.max(1f,measuredWidth /1f / width);
                setMeasuredDimension((int)(width* this.imageScale), (int) (height* this.imageScale));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(null!=currentFrame){
            canvas.save();
            matrix.setScale(imageScale, imageScale);
            canvas.drawBitmap(currentFrame,matrix,null);
            canvas.restore();
        }
    }

    /**
     * Release the resources in native.
     */
    public void release(){
        removeCallbacks(updateAction);
        nativeDecoder.free();
    }
}
