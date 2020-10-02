package com.cz.android.gif.sample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cz.android.gif.GifDecoder;

import java.io.File;
import java.io.IOException;

/**
 * An GIF view.
 * @see GifDecoder
 * @see com.cz.android.gif.GifHeaderDecoder
 */
public class GifView extends View {
    private static final String TAG="GifView";
    private GifDecoder decoder =new GifDecoder();
    private Matrix matrix=new Matrix();
    private Bitmap currentFrame;
    private int frameIndex;
    private int loopIndex;
    private boolean isRunning;
    private float scale=1f;
    private final Runnable updateAction=new Runnable() {
        @Override
        public void run() {
            if(isRunning){
                int frameCount = decoder.getFrameCount();
                if(frameIndex < frameCount){
                    long st = SystemClock.elapsedRealtime();
                    currentFrame = decoder.decodeFrame(frameIndex);
                    if(null!=currentFrame){
                        int delay = decoder.getDelayTime(frameIndex++);
//                        Log.i(TAG,"id:"+hashCode()+" frameIndex:"+frameIndex+" time:"+(SystemClock.elapsedRealtime()-st)+" delay:"+delay);
                        postDelayed(this,delay);
                        invalidate();
                    }
                } else {
                    frameIndex = 0;
                    int loopCount = decoder.getLoopCount();
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

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void loadImage(File imageFile) throws IOException {
        if(imageFile.exists()){
            decoder.loadFile(imageFile);
            requestLayout();
        }
    }

    public void loadImage(String filePath) throws IOException {
        loadImage(new File(filePath));
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start(){
        if(!isRunning){
            isRunning=true;
            frameIndex=0;
            loopIndex =0;
            int delayTime = decoder.getDelayTime(frameIndex);
            postDelayed(updateAction,delayTime);
        }
    }

    public void resume(){
        isRunning=true;
        post(updateAction);
    }

    public void pause(){
        isRunning=false;
        removeCallbacks(updateAction);
    }

    public void stop(){
        isRunning=false;
        frameIndex=0;
        currentFrame = null;
        removeCallbacks(updateAction);
    }

    public void release(){
        stop();
        decoder.close();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int width = decoder.getWidth();
        int height = decoder.getHeight();
        if(0 == width || 0 == height){
            setMeasuredDimension(0,0);
        } else {
            scale = Math.max(1f,measuredWidth * 1f / width);
            setMeasuredDimension((int)(width* this.scale), (int) (height* this.scale));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(null!=currentFrame){
            canvas.save();
            matrix.setScale(scale,scale);
            canvas.drawBitmap(currentFrame,matrix,null);
            canvas.restore();
        }
    }

}
