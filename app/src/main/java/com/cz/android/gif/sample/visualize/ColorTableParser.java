package com.cz.android.gif.sample.visualize;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cz.android.gif.GifDecoder;
import com.cz.android.gif.reader.Reader;
import com.cz.android.gif.sample.R;
import com.cz.android.gif.sample.extension.ByteTextView;
import com.cz.android.gif.sample.ui.view.ByteTextViewActivity;
import com.cz.android.gif.sample.visualize.view.GifBlockData;
import com.cz.android.gif.sample.visualize.view.GifColorBlockData;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by cz on 2020/9/19.
 * Process the global color table.
 * If the global color table is 1. This means we have a global color table.
 * @see LogicalScreenDescriptorBlockParser from this class you could find the flag: GloclColorTable
 * @see ImageDescriptorBlockParser from this class you could find the flag: localColorTableFlag
 * Also refer to the refreence file:document/spec-gif89a.txt
 *
 * iii) Global Color Table Flag - Flag indicating the presence of a
 *             Global Color Table; if the flag is set, the Global Color Table will
 *             immediately follow the Logical Screen Descriptor. This flag also
 *             selects the interpretation of the Background Color Index; if the
 *             flag is set, the value of the Background Color Index field should
 *             be used as the table index of the background color. (This field is
 *             the most significant bit of the byte.)
 *
 *             Values :    0 -   No Global Color Table follows, the Background
 *                               Color Index field is meaningless.
 *                         1 -   A Global Color Table will immediately follow, the
 *                               Background Color Index field is meaningful.
 *
 *
 *
 */
public class ColorTableParser extends GifBlockParser {
    private String[] attributeArray=new String[4];
    private String title;
    private int colorTableSize;

    public ColorTableParser() {
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setColorTableSize(int colorTableSize) {
        this.colorTableSize = colorTableSize;
    }

    @Override
    public boolean applyIdentifier(int b) {
        return true;
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse the ColorTable.");
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        GifColorBlockData blockData = new GifColorBlockData();
        blockData.title=title;
        blockData.colorTableSize=colorTableSize;
        blockData.start = reader.position();
        reader.skip(colorTableSize*3);
        blockData.end = reader.position();
        blockData.type=GifBlockParser.COLOR_TABLE_EXTENSION;
        return blockData;
    }

    @Override
    public View onCreateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.gif_color_table_extension_layout,parent,false);
    }

    @Override
    public void onBindView(final Context context, GifDecoder decoder, View layout, final GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
        int[] colorArray = readColorTable(byteBuffer, colorTableSize);

        //Generate the view by all the data.
        TextView titleView=layout.findViewById(R.id.titleView);
        titleView.setText(title);

        //Setting the file length and offset.
        TextView fileBlockLengthValue=layout.findViewById(R.id.fileBlockLengthValue);
        TextView fileBlockOffsetValue=layout.findViewById(R.id.fileBlockOffsetValue);
        fileBlockLengthValue.setText(String.valueOf(blockData.end-blockData.start));
        fileBlockOffsetValue.setText(String.valueOf(blockData.start));

        //Initialize the all the attributes of the data block.
        attributeArray[0] = "Color Count";
        attributeArray[1] = String.valueOf(colorArray.length);
        LinearLayout attributeLayout=layout.findViewById(R.id.attributeLayout);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //The signature and the version
        int requiredChildCount = 0 == attributeArray.length % 4 ? attributeArray.length/4 : attributeArray.length/4+1;
        int childCount = attributeLayout.getChildCount();
        int size = Math.max(requiredChildCount, childCount);
        for(int i=0;i<size;i++){
            if(i < childCount && i < requiredChildCount){
                View childView = attributeLayout.getChildAt(i);
                //initialize the child.
                childView.setVisibility(View.VISIBLE);
                initAttributeView(childView,attributeArray[i*ATTRIBUTE_COLUMN_SIZE],attributeArray[i*ATTRIBUTE_COLUMN_SIZE+1],attributeArray[i*ATTRIBUTE_COLUMN_SIZE+2],attributeArray[i*ATTRIBUTE_COLUMN_SIZE+3]);
            } else if(i >= childCount && i < requiredChildCount){
                //The extra view we don't need.
                addAttributeView(layoutInflater,attributeLayout,attributeArray[i*ATTRIBUTE_COLUMN_SIZE],attributeArray[i*ATTRIBUTE_COLUMN_SIZE+1],attributeArray[i*ATTRIBUTE_COLUMN_SIZE+2],attributeArray[i*ATTRIBUTE_COLUMN_SIZE+3]);
            } else if(i < childCount && i > requiredChildCount){
                View childView = attributeLayout.getChildAt(i);
                childView.setVisibility(View.GONE);
            }
        }

        ViewGroup colorTableLayout=layout.findViewById(R.id.colorTableLayout);
        int requiredColorCount = colorArray.length;
        int childColorCount = colorTableLayout.getChildCount();
        int colorSize = Math.max(requiredColorCount, childColorCount);
        for(int i=0;i<colorSize;i++){
            if(i < childColorCount && i < requiredColorCount){
                int color = colorArray[i];
                View childView = colorTableLayout.getChildAt(i);
                //initialize the child.
                childView.setVisibility(View.VISIBLE);
                initializeColorView(childView,color);
            } else if(i >= childColorCount && i < requiredColorCount){
                //The extra view we don't need.
                int color = colorArray[i];
                addColorView(layoutInflater,colorTableLayout,color);
            } else if(i < childColorCount && i > requiredColorCount){
                View childView = attributeLayout.getChildAt(i);
                childView.setVisibility(View.GONE);
            }
        }

        ByteTextView byteTextView=layout.findViewById(R.id.byteTextView);
        StringBuilder output = new StringBuilder();
        byteBuffer.flip();
        int length=Math.min(100,byteBuffer.limit());
        for(int i=0;i<length;i++){
            byte b = byteBuffer.get();
            CharSequence value = padStart(Integer.toHexString(b & 0xFF),2, '0').toString().toUpperCase();
            output.append(value);
        }
        byteTextView.setText(output);

        View byteButton=layout.findViewById(R.id.byteButton);
        byteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteTextViewActivity.Companion.startActivity(context,"ColorTable:"+blockData.start+"-"+blockData.end,blockData.file.getAbsolutePath(),blockData.start,blockData.end);
            }
        });
    }

    private void addColorView(LayoutInflater layoutInflater, ViewGroup colorTableLayout, int color) {
        View childView = layoutInflater.inflate(R.layout.gif_color_layout, colorTableLayout, false);
        childView.setBackgroundColor(color);
        TextView colorText1 = childView.findViewById(R.id.colorText1);
        TextView colorText2 = childView.findViewById(R.id.colorText2);
        colorText1.setText(String.valueOf(color));
        colorText2.setText(padStart(Integer.toHexString(color),6,'F').toString().toUpperCase());
        colorTableLayout.addView(childView);
    }

    private void initializeColorView(View childView, int color) {
        childView.setBackgroundColor(color);
        TextView colorText1 = childView.findViewById(R.id.colorText1);
        TextView colorText2 = childView.findViewById(R.id.colorText2);
        colorText1.setText(String.valueOf(color));
        colorText2.setText(padStart(Integer.toHexString(color),6,'F').toString().toUpperCase());
    }

    /**
     * Read color table.
     * When you start to read the global color table, or you guarantee you have a local color table.
     * Then you could start to parse the color table.
     * @param byteBuffer
     * @param colorTableSize
     * @return
     */
    private int[] readColorTable(ByteBuffer byteBuffer,int colorTableSize) {
        int[] colorTable=new int[colorTableSize];
        for(int i=0;i<colorTable.length;i++){
            int color=0;
            //The alpha color
            color|=0xFF000000;
            int r = byteBuffer.get() & 0xFF;
            //The red color
            color|=r<<16;
            int g = byteBuffer.get() & 0xFF;
            //The green color
            color|=g<<8;
            int b = byteBuffer.get() & 0xFF;
            //The blue color
            color|=b;
            colorTable[i]=color;
        }
        return colorTable;
    }
}
