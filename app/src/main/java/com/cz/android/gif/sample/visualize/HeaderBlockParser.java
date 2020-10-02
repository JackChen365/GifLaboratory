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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by cz on 2020/9/19.
 * Process the head of the file.
 * Determine if it is a Gif file or not. and display the version of the file.
 * Please refer to the resources file: document/spec-gif89a Header/7
 */
public class HeaderBlockParser extends GifBlockParser {
    private static final int HEADER_SIZE=6;
    private String[] attributeArray=new String[4];
    @Override
    public boolean applyIdentifier(int b) {
        return true;
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse the HeaderExtension.");
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        GifBlockData blockData = new GifBlockData();
        blockData.start = reader.position();
        reader.skip(HEADER_SIZE);
        blockData.end = reader.position();
        blockData.type=GifBlockParser.HEADER;
        return blockData;
    }

    @Override
    public View onCreateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.gif_header_layout,parent,false);
    }

    @Override
    public void onBindView(final Context context, GifDecoder decoder, View layout, final GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
        byte[] headArray=new byte[HEADER_SIZE];
        byteBuffer.get(headArray);
        String head = new String(headArray);
        String signature = head.substring(0,3);
        String version = head.substring(3);

        //Generate the view by all the data.
        TextView titleView=layout.findViewById(R.id.titleView);
        titleView.setText("Header");

        //Setting the file length and offset.
        TextView fileBlockLengthValue=layout.findViewById(R.id.fileBlockLengthValue);
        TextView fileBlockOffsetValue=layout.findViewById(R.id.fileBlockOffsetValue);
        fileBlockLengthValue.setText(String.valueOf(blockData.end-blockData.start));
        fileBlockOffsetValue.setText(String.valueOf(blockData.start));

        //Initialize the all the attributes of the data block.
        attributeArray[0] = "Signature";
        attributeArray[1] = signature;
        attributeArray[2] = "Version";
        attributeArray[3] = version;
        LinearLayout attributeLayout=layout.findViewById(R.id.attributeLayout);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
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

        ByteTextView byteTextView=layout.findViewById(R.id.byteTextView);
        StringBuilder output = new StringBuilder();
        byteBuffer.flip();
        for(int i=0;i<byteBuffer.limit();i++){
            byte b = byteBuffer.get();
            CharSequence value = padStart(Integer.toHexString(b & 0xFF),2, '0').toString().toUpperCase();
            output.append(value);
        }
        byteTextView.setText(output);

        View byteButton=layout.findViewById(R.id.byteButton);
        byteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteTextViewActivity.Companion.startActivity(context,"Header:"+blockData.start+"-"+blockData.end,blockData.file.getAbsolutePath(),blockData.start,blockData.end);
            }
        });
    }
}
