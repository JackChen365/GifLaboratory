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
 * Process the logical screen descriptor of the GIF file.
 * Please refer to the resources file: document/spec-gif89a Logical Screen Descriptor in page-8
 */
public class LogicalScreenDescriptorBlockParser extends GifBlockParser {
    private String[] attributeArray=new String[16];
    private int globalColorTableFlag;
    private int colorTableSize;
    @Override
    public boolean applyIdentifier(int identifier) {
        return false;
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse the LogicalScreenDescriptor.");
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        GifBlockData blockData = new GifBlockData();
        blockData.start = reader.position();
        //Skip the logical screen width and logical screen height
        reader.skip(4);
//<Packed Fields>  =      Global Color Table Flag       1 Bit
//                        Color Resolution              3 Bits
//                        Sort Flag                     1 Bit
//                        Size of Global Color Table    3 Bits
        byte packed = reader.readByte();
        //Skip two bytes which are the BackgroundIndex and the PixelAspectRatio
        reader.skip(2);
        globalColorTableFlag = (byte) ((packed>>7) & 0x01);
        byte globalColorTableSize= (byte) (packed & 0x07);
        colorTableSize = 1 << (globalColorTableSize+1);
        blockData.end = reader.position();
        blockData.type=GifBlockParser.LOGICAL_SCREEN_DESCRIPTOR;
        return blockData;
    }

    @Override
    public List<GifBlockParser> followingBlockParser(){
        List<GifBlockParser> blockParserList=null;
        if(1 == globalColorTableFlag){
            blockParserList = new ArrayList<>(1);
            ColorTableParser colorTableParser = new ColorTableParser();
            colorTableParser.setTitle("Global Color Table");
            colorTableParser.setColorTableSize(colorTableSize);
            blockParserList.add(colorTableParser);
        }
        return blockParserList;
    }

    @Override
    public View onCreateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        return layoutInflater.inflate( R.layout.gif_logical_screen_descriptor_layout,parent,false);
    }

    private short readShort(ByteBuffer byteBuffer){
        return (short) ((byteBuffer.get() & 0xFF) | ((byteBuffer.get() & 0xFF) << 8));
    }

    @Override
    public void onBindView(final Context context, GifDecoder decoder, View layout, final GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
//    i) Logical Screen Width - Width, in pixels, of the Logical Screen
//    where the images will be rendered in the displaying device.
        short width = readShort(byteBuffer);
        short height = readShort(byteBuffer);

//<Packed Fields>  =      Global Color Table Flag       1 Bit
//                        Color Resolution              3 Bits
//                        Sort Flag                     1 Bit
//                        Size of Global Color Table    3 Bits
        byte packed = byteBuffer.get();
        //11111111
        globalColorTableFlag = (byte) ((packed>>7) & 0x01);
        byte colorResolution  = (byte) ((packed>>4)&0x07);
        //turn 11110111 to 11110 by move to left 3 bit.
        byte sortFlag  = (byte) ((packed>>3) & 0x01);
        //The hex 0xf8 equal to binary value:11111000
        byte globalColorTableSize= (byte) (packed & 0x07);
        colorTableSize = 1<<(globalColorTableSize+1);

        byte backgroundColorIndex=byteBuffer.get();
        //If the value of the field is not 0, this approximation of the aspect ratio
        //is computed based on the formula:
        //Aspect Ratio = (Pixel Aspect Ratio + 15) / 64
        byte aspectRatioValue=byteBuffer.get();
        float aspectRatio=(aspectRatioValue+15)/64;

        TextView titleView=layout.findViewById(R.id.titleView);
        titleView.setText("Logical Screen Descriptor");

        //Setting the file length and offset.
        TextView fileBlockLengthValue=layout.findViewById(R.id.fileBlockLengthValue);
        TextView fileBlockOffsetValue=layout.findViewById(R.id.fileBlockOffsetValue);
        fileBlockLengthValue.setText(String.valueOf(blockData.end-blockData.start));
        fileBlockOffsetValue.setText(String.valueOf(blockData.start));

        //Initialize the all the attributes of the data block.
        LinearLayout attributeLayout=layout.findViewById(R.id.attributeLayout);
        attributeArray[0] = "Width";
        attributeArray[1] = String.valueOf(width);
        attributeArray[2] = "Height";
        attributeArray[3] = String.valueOf(height);
        attributeArray[4] = "Background index";
        attributeArray[5] = String.valueOf(backgroundColorIndex);
        attributeArray[6] = "Global Color Table";
        attributeArray[7] = String.valueOf(globalColorTableFlag);

        attributeArray[8] = "Color table size";
        attributeArray[9] = String.valueOf(globalColorTableSize);
        attributeArray[10] = "Color Resolution";
        attributeArray[11] = String.valueOf(colorResolution);
        attributeArray[12] = "Sorted Colors";
        attributeArray[13] = String.valueOf(sortFlag);
        attributeArray[14] = null;
        attributeArray[15] = null;

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
                ByteTextViewActivity.Companion.startActivity(context,"LocalScreenDescriptor:"+blockData.start+"-"+blockData.end,blockData.file.getAbsolutePath(),blockData.start,blockData.end);
            }
        });
    }
}
