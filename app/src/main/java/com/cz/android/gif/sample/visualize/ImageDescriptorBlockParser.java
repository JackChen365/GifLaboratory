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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cz on 2020/9/19.
 * Process the image descriptor of the file.
 * Please refer to the resources file: document/spec-gif89a
 * i) Image Separator - Identifies the beginning of an Image Descriptor. This field contains the fixed value 0x2C.
 */
public class ImageDescriptorBlockParser extends GifBlockParser {
    private static final int FIXED_IMAGE_DESCRIPTOR_SIZE=9;
    private final ImageDataBlockParser imageDataBlockParser=new ImageDataBlockParser();
    private String[] attributeArray=new String[20];
    private int localColorTableFlag;
    private int colorTableSize;
    @Override
    public boolean applyIdentifier(int b) {
        return 0x2C==b;
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse the ImageDescriptor.");
    }

    @Override
    public List<GifBlockParser> followingBlockParser() {
        List<GifBlockParser> blockParserList=new ArrayList<>(2);
        if(1==localColorTableFlag){
            ColorTableParser colorTableParser = new ColorTableParser();
            colorTableParser.setTitle("Local Color Table");
            colorTableParser.setColorTableSize(colorTableSize);
            blockParserList.add(colorTableParser);
        }
        blockParserList.add(imageDataBlockParser);
        return blockParserList;
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        GifBlockData blockData = new GifBlockData();
        blockData.start = reader.position();
        reader.skip(FIXED_IMAGE_DESCRIPTOR_SIZE-1);
        int packed=reader.readByteUnsigned();
        //Local Color Table Flag
        localColorTableFlag=packed>>7;
        //Size of Local Color Table 0x07 equal to the binary value:0000 0111
        int localColorTableSize=packed&0x07;
        colorTableSize = 1<<(localColorTableSize+1);
        blockData.end = reader.position();
        blockData.type=GifBlockParser.IMAGE_INSPECTOR;
        return blockData;
    }

    private short readShort(ByteBuffer byteBuffer){
        return (short) ((byteBuffer.get() & 0xFF) | ((byteBuffer.get() & 0xFF) << 8));
    }

    @Override
    public View onCreateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.gif_image_inspector_layout,parent,false);
    }

    @Override
    public void onBindView(final Context context, GifDecoder decoder, View layout, final GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
        short imageLeftPosition = readShort(byteBuffer);
        short imageTopPosition = readShort(byteBuffer);
        short imageWidth= readShort(byteBuffer);
        short imageHeight= readShort(byteBuffer);

        int packed=byteBuffer.get() & 0xFF;
//<Packed Fields>  =
//                Local Color Table Flag        1 Bit
//                Interlace Flag                1 Bit
//                Sort Flag                     1 Bit
//                Reserved                      2 Bits
//                Size of Local Color Table     3 Bits
        //Local Color Table Flag
        localColorTableFlag=packed>>7;
        //@see Appendix E for details.
        //Interlace Flag the value 01000000 equal to the hex value: 0x40
        int interlaceFlag=(packed&0x40) >> 6;
        //Sort Flag the value 00100000 equal to the hex value: 0x40
        int sortFlag=(packed&0x10)>>5;
        //The reserved bits.
        int reserved = (packed >> 2) & 0x03;
        //Size of Local Color Table 0x07 equal to the binary value:0000 0111
        int localColorTableSize=packed&0x07;
        colorTableSize = 1<<(localColorTableSize+1);


        TextView titleView=layout.findViewById(R.id.titleView);
        titleView.setText("Image descriptor");

        //Setting the file length and offset.
        TextView fileBlockLengthValue=layout.findViewById(R.id.fileBlockLengthValue);
        TextView fileBlockOffsetValue=layout.findViewById(R.id.fileBlockOffsetValue);
        fileBlockLengthValue.setText(String.valueOf(blockData.end-blockData.start));
        fileBlockOffsetValue.setText(String.valueOf(blockData.start));

        //Initialize the all the attributes of the data block.
        LinearLayout attributeLayout=layout.findViewById(R.id.attributeLayout);
        attributeArray[0] = "Left";
        attributeArray[1] = String.valueOf(imageLeftPosition);
        attributeArray[2] = "Top";
        attributeArray[3] = String.valueOf(imageTopPosition);
        attributeArray[4] = "Width";
        attributeArray[5] = String.valueOf(imageWidth);
        attributeArray[6] = "Height";
        attributeArray[7] = String.valueOf(imageHeight);

        attributeArray[8] = "Local Color Table";
        attributeArray[9] = String.valueOf(localColorTableFlag);
        attributeArray[10] = "Local Color Table Size";
        attributeArray[11] = String.valueOf(localColorTableSize);
        attributeArray[12] = "Interlaced";
        attributeArray[13] = String.valueOf(interlaceFlag);
        attributeArray[14] = "Color Sorted";
        attributeArray[15] = String.valueOf(sortFlag);

        attributeArray[16] = "Reserved";
        attributeArray[17] = String.valueOf(reserved);
        attributeArray[18] = null;
        attributeArray[19] = null;

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
                ByteTextViewActivity.Companion.startActivity(context,"ImageDescriptor:"+blockData.start+"-"+blockData.end,blockData.file.getAbsolutePath(),blockData.start,blockData.end);
            }
        });
    }
}
