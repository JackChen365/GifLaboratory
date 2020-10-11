package com.cz.android.gif.sample.ndk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Created by cz
 * @date 2020/9/30 20:06 PM
 * @email bingo110@126.com
 */
public class NativeTextureGifView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final int MAXIMUM_DELAY_TIME=1000;
    private static final String TAG="NativeTextureGifView";
    private static final int AVAILABLE_STATE=0x00;
    private static final int WORKING_STATE=0x01;
    private static final int PENDING_RELEASE_STATE=0x02;
    /**
     * The native decoder.
     */
    private NativeDecoder nativeDecoder=new NativeDecoder();
    private AnimationScheduler scheduler = AnimationScheduler.getDecoderScheduler();
    private AnimationCallback callback=new AnimationCallbackImpl();
    private OnRecycleListener listener;
    private Matrix matrix=new Matrix();
    private Bitmap currentFrame;
    private int frameIndex;
    private int loopIndex;
    private float imageScale =1f;
    private volatile boolean isRunning;
    /**
     * If the counter is equal to zero that means we can do our work,
     * Otherwise we have to wait, no matter you are trying to drawing something or releasing the resources.
     */
    private AtomicInteger lockState =new AtomicInteger();
    /**
     * Update frame action.
     */
    private Runnable updateAction=new Runnable() {
        @Override
        public void run() {
            if(isRunning&&isAvailable()&&isLockAvailable()){
                lockState.incrementAndGet();
                if(NativeDecoder.ANDROID_BITMAP_RESULT_SUCCESS==nativeDecoder.fillFrame(frameIndex,currentFrame)){
                    post(drawingAction);
                }
                //If the state beyond the WORKING_STATE, that means outside want to do something but failed.
                if(nativeDecoder.isReady()&&WORKING_STATE == lockState.get()){
                    short frameCount = nativeDecoder.getFrameCount();
                    if(1 < frameCount){
                        int delay = nativeDecoder.getDelay(frameIndex++);
                        scheduler.sentMessage(callback,AnimationScheduler.ACTION_UPDATE,delay);
                    }
                    lockState.decrementAndGet();
                }
            }
        }
    };

    /**
     * The drawing action, should always working on the main thread.
     */
    private Runnable drawingAction=new Runnable() {
        @Override
        public void run() {
            if(isAvailable()){
                Canvas canvas = lockCanvas();
                if(null!=canvas) {
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(currentFrame, matrix, null);
                }
                unlockCanvasAndPost(canvas);
            }
        }
    };

    public NativeTextureGifView(Context context) {
        super(context);
    }

    public NativeTextureGifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NativeTextureGifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Load the image from the given file.
     * @param imageFile
     */
    public void loadImage(File imageFile) {
        release();
        setSurfaceTextureListener(this);
        if(imageFile.exists()){
            if(nativeDecoder.loadFile(imageFile.getAbsolutePath())){
                int width = nativeDecoder.getWidth();
                int height = nativeDecoder.getHeight();
                currentFrame=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
                requestLayout();
            }
        }
    }

    public void loadImage(String filePath) {
        loadImage(new File(filePath));
    }

    public boolean isLockAvailable() {
        return AVAILABLE_STATE== lockState.get();
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
        long decoderPointer = nativeDecoder.getDecoderPointer();
        Log.i(TAG,"start:"+hashCode()+" decoderPointer:"+decoderPointer+" isRunning:"+isRunning+" isAvailable:"+isAvailable());
        if(!isRunning){
            frameIndex = 0;
            isRunning = true;
            listener = null;
            lockState.set(AVAILABLE_STATE);
            //If the surface is ready. start the animation.
            if(isAvailable()){
                scheduler.sentMessage(callback,AnimationScheduler.ACTION_START);
            }
        }
    }

    /**
     * Resume the animation if the animation if pause.
     */
    public void resume(){
        scheduler.sentMessage(callback,AnimationScheduler.ACTION_RESUME);
    }

    /**
     * Pause the animation. It usually cooperate with the method {@link #resume()}
     */
    public void pause(){
        isRunning=false;
        scheduler.sentMessage(callback,AnimationScheduler.ACTION_PAUSE);
    }

    /**
     * Stop the animation.
     */
    public void stop(){
        Log.i(TAG,"stop:"+hashCode());
        scheduler.sentMessage(callback,AnimationScheduler.ACTION_STOP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        if(nativeDecoder.isReady()){
            int measuredWidth = getMeasuredWidth();
            int width = nativeDecoder.getWidth();
            int height = nativeDecoder.getHeight();
            if(0 != width && 0 != height){
                imageScale = Math.max(1f,measuredWidth /1f / width);
                matrix.setScale(imageScale, imageScale);
                setMeasuredDimension((int)(width* this.imageScale), (int) (height* this.imageScale));
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //Invoke the method: start before the surface is available. Here we start the animation.
        if(isRunning()){
            frameIndex = 0;
            scheduler.sentMessage(callback,AnimationScheduler.ACTION_START);
            Log.i(TAG,"onSurfaceTextureAvailable:"+hashCode()+" isRunning:"+isRunning+" time:"+ SystemClock.uptimeMillis());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG,"onSurfaceTextureDestroyed:"+hashCode()+" thread:"+Thread.currentThread().getName());
        release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /**
     * Release the resources in native.
     */
    public boolean release(){
        if(WORKING_STATE == lockState.get()){
            //We have to wait.
            Log.i(TAG,"release:"+hashCode()+" wait:"+ lockState.get());
            lockState.incrementAndGet();
            return false;
        }
        if(PENDING_RELEASE_STATE== lockState.get()){
            lockState.set(AVAILABLE_STATE);
        }
        if(!nativeDecoder.isRecycled()){
            //Stop the animation.
            scheduler.sentMessage(callback,AnimationScheduler.ACTION_STOP);
            //Just release all the resources.
            lockState.incrementAndGet();
            Log.i(TAG,"release:"+hashCode()+" run.");
            isRunning=false;
            frameIndex = 0;
            nativeDecoder.free();
            scheduler.removeMessage(callback,AnimationScheduler.ACTION_START);
            scheduler.removeMessage(callback,AnimationScheduler.ACTION_UPDATE);
            lockState.decrementAndGet();

            if(null!=listener){
                listener.onRecycle();
            }
        }
        return true;
    }

    public void setOnRecycleListener(OnRecycleListener listener){
        this.listener=listener;
    }

    public interface OnRecycleListener{
        void onRecycle();
    }

    private class AnimationCallbackImpl implements AnimationCallback{

        @Override
        public void startAnimation() {
            isRunning=true;
            frameIndex=0;
            loopIndex =0;
        }

        @Override
        public void resumeAnimation() {
            isRunning=true;
        }

        @Override
        public void pauseAnimation() {
            isRunning=false;
        }

        @Override
        public void stopAnimation() {
            isRunning=false;
            frameIndex=0;
        }

        @Override
        public void updateAnimation(Executor executors) {
            if(isRunning()&&isAvailable()&&nativeDecoder.isReady()){
                int frameCount = nativeDecoder.getFrameCount();
                if(frameIndex < frameCount){
                    executors.execute(updateAction);
                } else {
                    frameIndex = 0;
                    int loopCount = nativeDecoder.getLoopCount();
                    int delayTime = nativeDecoder.getDelay(frameIndex);
                    if(0 == loopCount){
                        scheduler.sentMessage(callback,AnimationScheduler.ACTION_START,delayTime);
                    } else if(loopIndex < loopCount){
                        loopIndex++;
                        scheduler.sentMessage(callback,AnimationScheduler.ACTION_START,delayTime);
                    }
                }
            }
        }

        @Override
        public int hashCode() {
            return NativeTextureGifView.this.hashCode();
        }
    }

}
