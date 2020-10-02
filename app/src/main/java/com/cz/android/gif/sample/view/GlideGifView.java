package com.cz.android.gif.sample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.cz.android.gif.glide.BitmapProvider;
import com.cz.android.gif.glide.GifDecoder;
import com.cz.android.gif.glide.GifHeader;
import com.cz.android.gif.glide.GifHeaderParser;
import com.cz.android.gif.glide.StandardGifDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

/**
 * An simple GIF view basic on the Glide GIF decoder.
 */
public class GlideGifView extends View {
    private static final String TAG="GlideGifView";
    private GifDecoder decoder =new StandardGifDecoder(new BitmapProvider());
    private Matrix matrix=new Matrix();
    private Bitmap currentFrame;
    private int frameIndex;
    private int loopIndex;
    private boolean isRunning;
    private float scale=1f;
    private final Runnable updateAction=new Runnable() {
        @Override
        public void run() {
            int frameCount = decoder.getFrameCount();
            if(frameIndex < frameCount){
                long st = SystemClock.elapsedRealtime();
                currentFrame = decoder.getNextFrame();
                decoder.advance();
                if(null!=currentFrame){
                    int delay = decoder.getDelay(frameIndex++);
                    Log.i(TAG,"id:"+hashCode()+" frameIndex:"+frameIndex+" time:"+(SystemClock.elapsedRealtime()-st)+" delay:"+delay);
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
    };

    public GlideGifView(Context context) {
        super(context);
    }

    public GlideGifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlideGifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void loadImage(File imageFile) throws IOException {
        if(imageFile.exists()){
            try(FileChannel channel=new FileInputStream(imageFile).getChannel()){
                ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
                channel.read(byteBuffer);
                byte[] bytes = byteBuffer.array();
                GifHeaderParser headerParser = new GifHeaderParser();
                headerParser.setData(bytes);
                GifHeader header = headerParser.parseHeader();
                decoder.setData(header, bytes);
                decoder.advance();
                requestLayout();
            }

        }
    }

    public void loadImage(String filePath) throws IOException {
        loadImage(new File(filePath));
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start(){
        isRunning=true;
        frameIndex=0;
        loopIndex =0;
        int delayTime = decoder.getDelay(frameIndex);
        postDelayed(updateAction,delayTime);
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
        removeCallbacks(updateAction);
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

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }
}
