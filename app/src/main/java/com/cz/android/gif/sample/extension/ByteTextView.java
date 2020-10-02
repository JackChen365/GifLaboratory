package com.cz.android.gif.sample.extension;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.cz.android.gif.sample.R;

/**
 * Created by cz on 2020/9/30.
 * This is a view to display the byte. We decorate all the char sequence by {@link ByteReplacementSpan}
 * And we support some features like highlight the text.
 *
 * @see ByteReplacementSpan
 */
public class ByteTextView extends AppCompatTextView {
    /**
     * The start offset position
     */
    private int selectStart=0;
    /**
     * Select path
     */
    private Path selectPath=new Path();
    /**
     * The paint
     */
    private Paint selectPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * Select text foreground span
     */
    private ForegroundColorSpan selectForegroundColorSpan=null;

    public ByteTextView(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public ByteTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ByteTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(@NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(null, new int[] { R.attr.colorAccent });
        int accentColor = a.getColor(0, 0);
        a.recycle();
        selectPaint.setColor(accentColor);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        //Marked this text as a spannable. In this case we will be able to change the span
        SpannableString spannableString = new SpannableString(text);
        for(int i=0;i<text.length();i+=2){
            spannableString.setSpan(new ByteReplacementSpan(),i,Math.min(text.length(),i+2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.setText(spannableString, BufferType.SPANNABLE);
    }

    /**
     * Highlight the text programmatically.
     * @param start
     * @param end
     */
    public void setSelectText(int start, int end){
        Layout layout = getLayout();
        if(null!=layout){
            selectStart = start;
            layout.getSelectionPath(start, end, selectPath);
            //reset the selected text foreground color
            CharSequence text = getText();
            if(text instanceof Spannable){
                Spannable spannable = (Spannable) text;
                selectForegroundColorSpan=new ForegroundColorSpan(Color.WHITE);
                spannable.setSpan(selectForegroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        if(MotionEvent.ACTION_DOWN==action){
            ViewParent parent = getParent();
            parent.requestDisallowInterceptTouchEvent(true);
            addSelection(x-getPaddingLeft(), y-getPaddingTop());
        } else if(MotionEvent.ACTION_MOVE==action){
            updateSelection(x-getPaddingLeft(), y-getPaddingTop());
        }
        return true;
    }

    private void addSelection(float x,float y) {
        Layout layout = getLayout();
        if (null != layout) {
            CharSequence text=getText();
            if(text instanceof Spannable &&null!=selectForegroundColorSpan){
                Spannable spannable = (Spannable) text;
                spannable.removeSpan(selectForegroundColorSpan);
            }
            int line = layout.getLineForVertical((int) y);
            selectStart = layout.getOffsetForHorizontal(line, x);
            if(selectStart < text.length()){
                layout.getSelectionPath(selectStart, selectStart + 1, selectPath);
                //reset the selected span object
                if(text instanceof Spannable){
                    Spannable spannable = (Spannable) text;
                    selectForegroundColorSpan=new ForegroundColorSpan(Color.WHITE);
                    spannable.setSpan(selectForegroundColorSpan, selectStart, selectStart+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                invalidate();
            }
        }
    }

    /**
     * Update the selected range
     */
    private void updateSelection(float x,float y) {
        Layout layout=getLayout();
        if (null != layout) {
            CharSequence text = getText();
            if(text instanceof Spannable&&null!=selectForegroundColorSpan){
                Spannable spannable = (Spannable) text;
                spannable.removeSpan(selectForegroundColorSpan);
            }
            int line = layout.getLineForVertical((int) y);
            int off = layout.getOffsetForHorizontal(line, x);
            layout.getSelectionPath(selectStart, off, selectPath);

            if(text instanceof Spannable){
                Spannable spannable = (Spannable) text;
                selectForegroundColorSpan=new ForegroundColorSpan(Color.WHITE);
                int offset=Math.min(off,text.length());
                int start=Math.min(selectStart,offset);
                int end=Math.max(selectStart,offset);
                spannable.setSpan(selectForegroundColorSpan,start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft()*1f,getPaddingTop()*1f);
        canvas.drawPath(selectPath,selectPaint);
        canvas.restore();
        super.onDraw(canvas);
    }



}
