package com.cz.android.gif.sample.visualize.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Created by cz
 * @date 2020/9/25 5:54 PM
 * @email bingo110@126.com
 */
public class ScaleImageView extends View {
    private static final float MAXIMUM_SCALE=10f;
    private Matrix matrix=new Matrix();
    private Bitmap bitmap;
    private float scale=1f;

    public ScaleImageView(@NonNull Context context) {
        super(context);
    }

    public ScaleImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageBitmap(Bitmap bitmap){
        this.bitmap=bitmap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if(null!=bitmap){
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            scale = Math.min(MAXIMUM_SCALE,Math.max(1f,measuredWidth/2f / bitmapWidth));
            matrix.setScale(scale,scale);
            setMeasuredDimension((int)(bitmapWidth* scale), (int) (bitmapHeight* scale));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(null!=bitmap){
            canvas.save();
            matrix.setScale(scale,scale);
            canvas.drawBitmap(bitmap,matrix,null);
            canvas.restore();
        }
    }
}
