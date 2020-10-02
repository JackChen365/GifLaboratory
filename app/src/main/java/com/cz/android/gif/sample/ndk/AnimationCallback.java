package com.cz.android.gif.sample.ndk;

import java.util.concurrent.Executor;

public interface AnimationCallback {
    void startAnimation();
    void resumeAnimation();
    void pauseAnimation();
    void stopAnimation();
    void updateAnimation(Executor executors);
}