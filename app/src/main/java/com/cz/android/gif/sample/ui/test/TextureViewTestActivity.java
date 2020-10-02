package com.cz.android.gif.sample.ui.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.cz.android.sample.api.Exclude;

@Exclude
public class TextureViewTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FrameLayout parentView=new FrameLayout(this);
        setContentView(parentView);
        final MyTextureView myTextureView = new MyTextureView(this);
        parentView.addView(myTextureView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);


        Button button = new Button(this);
        button.setText("test");
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity= Gravity.RIGHT|Gravity.BOTTOM;
        parentView.addView(button,layoutParams);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTextureView.startTest();
                myTextureView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        parentView.removeView(myTextureView);
                    }
                },1000);
            }
        });
    }

    public static class MyTextureView extends TextureView{

        public MyTextureView(Context context) {
            super(context);
        }

        public MyTextureView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void startTest() {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    Canvas canvas = lockCanvas();
                    SystemClock.sleep(3000);
                    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setColor(Color.RED);
                    canvas.drawText("ABCD",0,0,paint);
                    unlockCanvasAndPost(canvas);
                }
            }.start();
        }
    }
}