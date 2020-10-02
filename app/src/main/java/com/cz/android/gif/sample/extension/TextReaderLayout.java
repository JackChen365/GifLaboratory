package com.cz.android.gif.sample.extension;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TextReaderLayout extends FrameLayout {
    private ByteTextView textView;
    private FileChannel channel;
    private ByteBuffer byteBuffer;
    private long startOffset;
    private long endOffset;
    /**
     * Current load page index.
     */
    private int currentPage;
    /**
     * The total page byte size.
     */
    private int pageByteSize;
    /**
     * The total page size.
     */
    private int pageSize;

    private boolean dataDirty;

    public TextReaderLayout(@NonNull Context context) {
        this(context,null,0);
        initializeTextView();
    }

    public TextReaderLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TextReaderLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initializeTextView();
    }

    private void initializeTextView() {
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            findTextView(childView);
        }
        if(null==textView){
            Context context = getContext();
            textView=new ByteTextView(context);
            addView(textView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private void findTextView(View view){
        if(view instanceof ByteTextView){
            textView= (ByteTextView) view;
        } else if(view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for(int i=0;i<childCount;i++){
                View childView = viewGroup.getChildAt(i);
                findTextView(childView);
            }
        }
    }

    public void loadFile(String filePath) throws IOException {
        loadFile(new File(filePath));
    }

    /**
     * Load data by the given file.
     * We will open this file and keep it until the view detach from the window.
     * @param file
     * @throws IOException
     */
    public void loadFile(File file) throws IOException {
        if(file.exists()){
            FileInputStream fileInputStream = new FileInputStream(file);
            channel = fileInputStream.getChannel();
            this.dataDirty=true;
            if(0 == startOffset && 0 == endOffset){
                this.startOffset=0;
                this.endOffset=channel.size();
            }
            requestLayout();
        }
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageByteSize() {
        return pageByteSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(null!=channel){
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(null!=byteBuffer){
            byteBuffer=null;
        }
    }

    public void setStartOffset(int start){
        this.startOffset=start;
    }

    public void setEndOffset(int end){
        this.endOffset=end;
    }

    public void setCurrentPage(int progress) {
        currentPage = progress;
        updateText(progress);
    }

    public void nextPage(){
        if(currentPage+1 < pageSize){
            updateText(++currentPage);
        }
    }

    public void previousPage(){
        if(currentPage-1 >= 0){
            updateText(--currentPage);
        }
    }

    /**
     * Update the TextView inside this layout.
     * @see #previousPage()
     * @see #nextPage()
     */
    private void updateText(int currentPage) {
        int textLength = Math.min(pageByteSize,(int)(endOffset-startOffset));
        if(null==byteBuffer||byteBuffer.limit() < textLength){
            byteBuffer = ByteBuffer.allocate(textLength);
        }
        StringBuilder output=new StringBuilder();
        try {
            long start=startOffset+currentPage*pageByteSize;
            byteBuffer.clear();
            channel.position(start);
            channel.read(byteBuffer);
            byteBuffer.flip();
            for (int i=0;i<byteBuffer.limit();i++) {
                byte b = byteBuffer.get();
                output.append(padStart(Integer.toHexString(b & 0xFF),2, '0').toString().toUpperCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.setText(output);
    }

    private CharSequence padStart(CharSequence text,int length,char padChar){
        if (length < 0)
            throw new IllegalArgumentException("Desired length $length is less than zero.");
        if (length <= text.length())
            return text.subSequence(0, text.length());

        StringBuilder sb = new StringBuilder(length);
        for (int i=1;i<=(length - text.length());i++)
            sb.append(padChar);
        sb.append(text);
        return sb;
    }

    @Override
    public void requestLayout() {
        //Make the data dirty. If the dimension or something changed.
        dataDirty=true;
        super.requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //If the data is dirty. We re-calculate the page information.
        if(dataDirty){
            dataDirty = true;
            measurePage(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Measure the page information
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void measurePage(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = textView.getPaddingLeft();
        int paddingTop = textView.getPaddingTop();
        int paddingRight = textView.getPaddingRight();
        int paddingBottom = textView.getPaddingBottom();
        int measuredWidth = textView.getMeasuredWidth();
        int measuredHeight = textView.getMeasuredHeight();
        String defaultText = ByteReplacementSpan.DEFAULT_TEXT;
        TextPaint textPaint = textView.getPaint();
        int textWidth = Math.round(textPaint.measureText(defaultText));
        int lineByteSize = (measuredWidth-paddingLeft-paddingRight) / textWidth;

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        int lineCount = Math.round((measuredHeight-paddingTop-paddingBottom) / textHeight);

        int byteSize = lineByteSize * lineCount;
        long textLength = (endOffset - startOffset);
        //Calculate how many byte we could have inside one page.
        this.pageByteSize=byteSize;
        //How many page we have.
        this.pageSize= (int) (textLength/byteSize);
        //Load text
        updateText(currentPage);
    }
}
