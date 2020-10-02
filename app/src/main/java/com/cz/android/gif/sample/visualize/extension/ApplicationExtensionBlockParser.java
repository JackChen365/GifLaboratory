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
 * Process the application extension of the file.
 * Please refer to the resources file: document/spec-gif89a Application Extension/26
 */
public class ApplicationExtensionBlockParser extends GifBlockParser {
//        iii) Block Size - Number of bytes in this extension block,
//        following the Block Size field, up to but not including the
//        beginning of the Application Data. This field contains the fixed
//        value 11.
    private String[] attributeArray=new String[8];

    @Override
    public boolean applyIdentifier(int b) {
        return 0xFF==b;
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse the ApplicationExtension.");
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        GifBlockData blockData = new GifBlockData();
        blockData.start = reader.position();
        int blockSize = reader.readByteUnsigned();
        reader.skip(blockSize);
        int subBlockSize=reader.readByteUnsigned();
        reader.skip(subBlockSize);
        while(0 != subBlockSize){
            subBlockSize=reader.readByteUnsigned();
            reader.skip(subBlockSize);
        }
        blockData.end = reader.position();
        blockData.type=GifBlockParser.APPLICATION_EXTENSION;
        return blockData;
    }

    @Override
    public View onCreateView(Context context,LayoutInflater layoutInflater,ViewGroup parent) {
        return layoutInflater.inflate(R.layout.gif_app_extension_layout,parent,false);
    }

    @Override
    public void onBindView(final Context context, GifDecoder decoder, View layout, final GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
//        iii) Block Size - Number of bytes in this extension block,
//        following the Block Size field, up to but not including the
//        beginning of the Application Data. This field contains the fixed
//        value 11.
        final int blockSize = byteBuffer.get() & 0xFF;

//        iv) Application Identifier - Sequence of eight printable ASCII
//        characters used to identify the application owning the Application
//        Extension.
        byte[] applicationIdentifierArray=new byte[8];
        byteBuffer.get(applicationIdentifierArray);
        String applicationIdentifier = new String(applicationIdentifierArray);

//        v) Application Authentication Code - Sequence of three bytes used
//        to authenticate the Application Identifier. An Application program
//        may use an algorithm to compute a binary code that uniquely
//        identifies it as the application owning the Application Extension.
        byte[] authenticationCodeArray=new byte[3];
        byteBuffer.get(authenticationCodeArray);
        String authenticationCode = new String(authenticationCodeArray);

        int blockTotalSize=byteBuffer.remaining();
        //Generate the view by all the data.
        TextView titleView=layout.findViewById(R.id.titleView);
        titleView.setText("Application Extension");

        //Setting the file length and offset.
        TextView fileBlockLengthValue=layout.findViewById(R.id.fileBlockLengthValue);
        TextView fileBlockOffsetValue=layout.findViewById(R.id.fileBlockOffsetValue);
        fileBlockLengthValue.setText(String.valueOf(blockData.end-blockData.start));
        fileBlockOffsetValue.setText(String.valueOf(blockData.start));

        //Initialize the all the attributes of the data block.
        attributeArray[0] = "Block Size";
        attributeArray[1] = String.valueOf(blockSize);
        attributeArray[2] = "Application Identifier";
        attributeArray[3] = applicationIdentifier;
        attributeArray[4] = "Authentication Code";
        attributeArray[5] = authenticationCode;
        attributeArray[6] = "Block Total Size";
        attributeArray[7] = String.valueOf(blockTotalSize);

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
                ByteTextViewActivity.Companion.startActivity(context,"Application Extension:"+blockData.start+"-"+blockData.end,blockData.file.getAbsolutePath(),blockData.start,blockData.end);
            }
        });

    }
}
