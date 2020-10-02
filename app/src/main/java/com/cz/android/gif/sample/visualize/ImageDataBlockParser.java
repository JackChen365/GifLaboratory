package com.cz.android.gif.sample.visualize;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cz.android.gif.GifDecoder;
import com.cz.android.gif.reader.Reader;
import com.cz.android.gif.sample.R;
import com.cz.android.gif.sample.extension.ByteTextView;
import com.cz.android.gif.sample.ui.view.ByteTextViewActivity;
import com.cz.android.gif.sample.visualize.view.GifBlockData;
import com.cz.android.gif.sample.visualize.view.GifImageBlockData;
import com.cz.android.gif.sample.visualize.view.ScaleImageView;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by cz on 2020/9/19.
 * Process the Table Based Image Data. of the file.
 * Please refer to the resources file: document/spec-gif89a
 * After the image descriptor we start to process the image data.
 */
public class ImageDataBlockParser extends GifBlockParser {
    private String[] attributeArray=new String[8];
    private int frameIndex;
    @Override
    public boolean applyIdentifier(int b) {
        return 0x2C==b;
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse the ImageData.");
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        GifImageBlockData blockData = new GifImageBlockData();
        blockData.index=frameIndex++;
        blockData.start = reader.position();
        int lzwMinimumCodeSize = reader.readByteUnsigned();
        int blockSize=reader.readByteUnsigned();
        reader.skip(blockSize);
        while(0 != blockSize){
            blockSize=reader.readByteUnsigned();
            reader.skip(blockSize);
        }
        blockData.end = reader.position();
        blockData.type=GifBlockParser.IMAGE_DATA_BLOCK;
        return blockData;
    }

    @Override
    public View onCreateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.gif_image_data_layout,parent,false);
    }

    @Override
    public void onBindView(final Context context, GifDecoder decoder, View layout, final GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
        int lzwMinimumCodeSize = byteBuffer.get() & 0xFF;
        int codeSize=lzwMinimumCodeSize+1;
        int clearCode = (1 << lzwMinimumCodeSize)-1;
        int endOfInformation=clearCode+1;

        int blockSize = byteBuffer.get() & 0xFF;
        byteBuffer.position(byteBuffer.position()+blockSize);
        int blockTotalSize=blockSize;
        while(0 != blockSize){
            blockSize = byteBuffer.get() & 0xFF;
            byteBuffer.position(byteBuffer.position()+blockSize);
            blockTotalSize += blockSize;
        }

        //Generate the view by all the data.
        TextView titleView=layout.findViewById(R.id.titleView);
        titleView.setText("Image Data");

        //Setting the file length and offset.
        TextView fileBlockLengthValue=layout.findViewById(R.id.fileBlockLengthValue);
        TextView fileBlockOffsetValue=layout.findViewById(R.id.fileBlockOffsetValue);
        fileBlockLengthValue.setText(String.valueOf(blockData.end-blockData.start));
        fileBlockOffsetValue.setText(String.valueOf(blockData.start));

        //Initialize the all the attributes of the data block.
        attributeArray[0] = "LZW Min Code Size";
        attributeArray[1] = String.valueOf(lzwMinimumCodeSize);
        attributeArray[2] = "Clear Code";
        attributeArray[3] = String.valueOf(clearCode);
        attributeArray[4] = "End of Information";
        attributeArray[5] = String.valueOf(endOfInformation);
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
        //Display the image.
        GifImageBlockData imageBlockData = (GifImageBlockData) blockData;
        ScaleImageView imageView=layout.findViewById(R.id.imageView);
        Bitmap bitmap = decoder.decodeFrame(imageBlockData.index);
        imageView.setImageBitmap(bitmap);

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

        View codeStreamButton=layout.findViewById(R.id.codeStreamButton);
        codeStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        View codeTableButton=layout.findViewById(R.id.codeTableButton);
        codeTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        View byteButton=layout.findViewById(R.id.byteButton);
        byteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteTextViewActivity.Companion.startActivity(context,"ImageDataBlock:"+blockData.start+"-"+blockData.end,blockData.file.getAbsolutePath(),blockData.start,blockData.end);
            }
        });
    }
}
