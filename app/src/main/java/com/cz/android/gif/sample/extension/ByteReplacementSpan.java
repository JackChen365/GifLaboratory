package com.cz.android.gif.sample.extension;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by cz on 2020/9/30.
 * This is a span to display the byte. it splits all the word into two words.
 * For example:FF FF 3A FF
 */
public class ByteReplacementSpan extends ReplacementSpan {
    public static final String DEFAULT_TEXT="000";

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(DEFAULT_TEXT));
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        int spanSize = getSize(paint, text, start, end, null);
        float textWidth = paint.measureText(text, start, end);
        canvas.drawText(text,start,end,x+(spanSize-textWidth)/2,y,paint);
    }
}
