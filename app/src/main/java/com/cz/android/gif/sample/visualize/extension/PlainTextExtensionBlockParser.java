package com.cz.android.gif.sample.visualize.extension;

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
import com.cz.android.gif.sample.visualize.GifBlockParser;
import com.cz.android.gif.sample.visualize.view.GifBlockData;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by cz on 2020/9/19.
 * Process the plain text block of the file.
 * Please refer to the resources file: document/spec-gif89a Plain Text Extension/25
 *
 * i) Extension Introducer - Identifies the beginning of an extension
 * block. This field contains the fixed value 0x21.
 *
 * ii) Plain Text Label - Identifies the current block as a Plain Text
 * Extension. This field contains the fixed value 0x01.
 */
public class PlainTextExtensionBlockParser extends GifBlockParser {
    private String[] attributeArray=new String[20];
    @Override
    public boolean applyIdentifier(int b) {
        return 0x01==b;
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse PlainTextExtension.");
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        GifBlockData blockData = new GifBlockData();
        blockData.start = reader.position();
        int blockSize = reader.readByteUnsigned();
        reader.skip(blockSize);
        blockData.end = reader.position();
        blockData.type=GifBlockParser.PLAIN_TEXT_EXTENSION;
        return blockData;
    }

    private short readShort(ByteBuffer byteBuffer){
        return (short) ((byteBuffer.get() & 0xFF) | ((byteBuffer.get() & 0xFF) << 8));
    }

    @Override
    public View onCreateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.gif_header_layout,parent,false);
    }

    @Override
    public void onBindView(final Context context, GifDecoder decoder, View layout, final GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
        //Block Size
        int plainTextBlockSize=byteBuffer.get() & 0xFF;

        //The Text Grid Left Position
        short textGridLeftPosition=readShort(byteBuffer);
        //The Text Grid Top Position
        short textGridTopPosition=readShort(byteBuffer);

        //Text Grid Width
        short gridWidth=readShort(byteBuffer);
        //Text Grid Height
        short gridHeight=readShort(byteBuffer);

        //Character Cell Width
        int characterCellWidth=byteBuffer.get() & 0xFF;
        //Character Cell Height
        int characterCellHeight=byteBuffer.get() & 0xFF;

        //Text Foreground Color Index
        int textForegroundColorIndex = byteBuffer.get() & 0xFF;
        //Text Background Color Index
        int  textBackgroundColorIndex = byteBuffer.get() & 0xFF;

        //Generate the view by all the data.
        TextView titleView=layout.findViewById(R.id.titleView);
        titleView.setText("Plain Text Extension.");

        //Setting the file length and offset.
        TextView fileBlockLengthValue=layout.findViewById(R.id.fileBlockLengthValue);
        TextView fileBlockOffsetValue=layout.findViewById(R.id.fileBlockOffsetValue);
        fileBlockLengthValue.setText(String.valueOf(blockData.end-blockData.start));
        fileBlockOffsetValue.setText(String.valueOf(blockData.start));

        //Initialize the all the attributes of the data block.
        attributeArray[0] = "Block Size";
        attributeArray[1] = "Block Size";
        attributeArray[2] = null;
        attributeArray[3] = null;
        attributeArray[4] = "Grid Width";
        attributeArray[5] = String.valueOf(gridWidth);
        attributeArray[6] = "Grid Height";
        attributeArray[7] = String.valueOf(gridHeight);

        attributeArray[8] = "Grid Left Position";
        attributeArray[9] = String.valueOf(textGridLeftPosition);
        attributeArray[10] = "Grid Top Position";
        attributeArray[11] = String.valueOf(textGridTopPosition);
        attributeArray[12] = "Character Cell Width";
        attributeArray[13] = String.valueOf(characterCellWidth);
        attributeArray[14] = "Character Cell Height";
        attributeArray[15] = String.valueOf(characterCellHeight);

        attributeArray[16] = "Text Foreground Color Index";
        attributeArray[17] = String.valueOf(textForegroundColorIndex);
        attributeArray[18] = "Text Background Color Index";
        attributeArray[19] = String.valueOf(textBackgroundColorIndex);

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
                ByteTextViewActivity.Companion.startActivity(context,"Plain Text Extension:"+blockData.start+"-"+blockData.end,blockData.file.getAbsolutePath(),blockData.start,blockData.end);
            }
        });
    }
}
