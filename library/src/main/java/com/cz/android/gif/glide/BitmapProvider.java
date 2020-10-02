package com.cz.android.gif.glide;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class BitmapProvider implements GifDecoder.BitmapProvider {

    @NonNull
    @Override
    public Bitmap obtain(int width, int height, Bitmap.Config config) {
      Bitmap result = Bitmap.createBitmap(width, height, config);
      return result;
    }

    @Override
    public void release(@NonNull Bitmap bitmap) {
      // Do nothing.
    }

    @NonNull
    @Override
    public byte[] obtainByteArray(int size) {
      return new byte[size];
    }

    @Override
    public void release(@NonNull byte[] bytes) {
      // Do nothing.
    }

    @NonNull
    @Override
    public int[] obtainIntArray(int size) {
      return new int[size];
    }

    @Override
    public void release(@NonNull int[] array) {
      // Do Nothing
    }

  }