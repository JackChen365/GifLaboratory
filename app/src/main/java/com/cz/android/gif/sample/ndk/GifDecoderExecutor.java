package com.cz.android.gif.sample.ndk;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Created by cz
 * @date 2020/9/30 4:08 PM
 * @email bingo110@126.com
 */
public class GifDecoderExecutor implements Executor {
    private final Executor executor;

    public GifDecoderExecutor() {
        this(Runtime.getRuntime().availableProcessors()+1);
    }

    public GifDecoderExecutor(int coreSize) {
        executor= Executors.newFixedThreadPool(coreSize);
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}
