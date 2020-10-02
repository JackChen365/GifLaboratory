package com.cz.android.gif.sample.ndk;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Created by cz
 * @date 2020/9/30 21:14 PM
 * @email bingo110@126.com
 *
 * We use this handle thread to update the image no matter how many views we need to process.
 * But decode the image in {@link GifDecoderExecutor}.
 *
 * For example:
 * We have four views that need to display.
 *
 * The first one update every 100ms. another one update every 50ms.
 * We use an executor to decode the image. and this class helps us to manage all the views.
 *
 */
public class AnimationScheduler extends HandlerThread implements Handler.Callback {
    private static final String TAG="AnimationScheduler";
    /**
     * Indicate we need to update the image.
     */
    public static final int ACTION_UPDATE=0x00;
    /**
     * Indicate we need to start the animation.
     */
    public static final int ACTION_START=0x01;
    /**
     * Indicate we need to resume the animation.
     */
    public static final int ACTION_RESUME=0x02;
    /**
     * Indicate we need to pause the animation.
     */
    public static final int ACTION_PAUSE=0x03;
    /**
     * Indicate we need to stop the animation.
     */
    public static final int ACTION_STOP=0x04;
    /**
     * The unique scheduler.
     */
    private static final AnimationScheduler decoderScheduler=new AnimationScheduler();
    /**
     * The default executor for this view to decode the Gif image.
     */
    private static final Executor executor=new GifDecoderExecutor();
    /**
     * The counter used to help us to know how many message we have.
     */
    private AtomicInteger messageCounter=new AtomicInteger();

    public static AnimationScheduler getDecoderScheduler(){
        if(!decoderScheduler.isAlive()){
            decoderScheduler.start();
            //initialize the handle.
            Looper looper = decoderScheduler.getLooper();
            decoderScheduler.handler=new Handler(looper,decoderScheduler);
        }
        return decoderScheduler;
    }
    /**
     * The update callback list.
     */
    private List<AnimationCallback> updateCallbackList=new ArrayList<>();

    /**
     * The handle only for this thread.
     */
    private Handler handler;

    public AnimationScheduler() {
        super("GifDecoderScheduler");
    }

    public void sentMessage(AnimationCallback callback,int what){
        Message message = Message.obtain();
        message.what=what;
        message.obj = callback;
        handler.sendMessage(message);
        messageCounter.incrementAndGet();
    }

    public void sentMessage(AnimationCallback callback,int what,long delayMillis){
        Message message = Message.obtain();
        message.what=what;
        message.obj = callback;
        handler.sendMessageDelayed(message,delayMillis);
        messageCounter.incrementAndGet();
    }

    public void removeMessage(Object obj,int what){
        handler.removeMessages(what,obj);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        messageCounter.decrementAndGet();
        if(ACTION_START==msg.what){
            AnimationCallback callback = (AnimationCallback) msg.obj;
            callback.startAnimation();
            callback.updateAnimation(executor);
        } else if(ACTION_UPDATE==msg.what){
            AnimationCallback callback = (AnimationCallback) msg.obj;
            callback.updateAnimation(executor);
        } else if(ACTION_RESUME ==msg.what){
            AnimationCallback callback = (AnimationCallback) msg.obj;
            callback.resumeAnimation();
            callback.updateAnimation(executor);
        } else if(ACTION_PAUSE ==msg.what){
            AnimationCallback callback = (AnimationCallback) msg.obj;
            callback.pauseAnimation();
        } else if(ACTION_STOP ==msg.what){
            AnimationCallback callback = (AnimationCallback) msg.obj;
            callback.stopAnimation();
        }
        return true;
    }
}
