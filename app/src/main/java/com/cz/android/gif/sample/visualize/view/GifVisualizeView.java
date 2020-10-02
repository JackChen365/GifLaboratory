package com.cz.android.gif.sample.visualize.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cz.android.gif.GifDecoder;
import com.cz.android.gif.reader.BufferedChannelReader;
import com.cz.android.gif.reader.Reader;
import com.cz.android.gif.sample.visualize.GifBlockParser;
import com.cz.android.gif.sample.visualize.HeaderBlockParser;
import com.cz.android.gif.sample.visualize.ImageDescriptorBlockParser;
import com.cz.android.gif.sample.visualize.LogicalScreenDescriptorBlockParser;
import com.cz.android.gif.sample.visualize.TerminatorBlockParser;
import com.cz.android.gif.sample.visualize.extension.ApplicationExtensionBlockParser;
import com.cz.android.gif.sample.visualize.extension.CommentExtensionBlockParser;
import com.cz.android.gif.sample.visualize.extension.GraphicsControlExtensionParser;
import com.cz.android.gif.sample.visualize.extension.PlainTextExtensionBlockParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GifVisualizeView extends FrameLayout {
    private static final String TAG="GifVisualizeView";
    private ImageDescriptorBlockParser imageDescriptorBlockParser = new ImageDescriptorBlockParser();
    private GraphicsControlExtensionParser graphicsControlExtensionParser = new GraphicsControlExtensionParser();
    private CommentExtensionBlockParser commentExtensionBlockParser = new CommentExtensionBlockParser();
    private PlainTextExtensionBlockParser plainTextExtensionBlockParser = new PlainTextExtensionBlockParser();
    private ApplicationExtensionBlockParser applicationExtensionBlockParser = new ApplicationExtensionBlockParser();
    private RecyclerView recyclerView;

    public GifVisualizeView(Context context) {
        super(context);
        initialize(context);
    }

    public GifVisualizeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public GifVisualizeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        recyclerView = new RecyclerView(context);
        super.addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void loadImage(String filePath) throws IOException {
        File imageFile=new File(filePath);
        if(imageFile.exists()){
            loadImage(imageFile);
        }
    }

    public void loadImage(File imageFile) throws IOException {
        if(imageFile.exists()){
            long st = SystemClock.elapsedRealtime();
            List<GifBlockData> blockDataList=new ArrayList<>();
            FileInputStream fileInputStream=new FileInputStream(imageFile);
            try(BufferedChannelReader reader=new BufferedChannelReader(fileInputStream.getChannel())) {
                //Step1: process the header
                final HeaderBlockParser headerBlockParser = new HeaderBlockParser();
                blockDataList.add(headerBlockParser.readBlock(reader));
                //Step2: process logical screen descriptor.
                LogicalScreenDescriptorBlockParser logicalScreenDescriptorBlockParser = new LogicalScreenDescriptorBlockParser();
                blockDataList.add(logicalScreenDescriptorBlockParser.readBlock(reader));
                List<GifBlockParser> blockParserList = logicalScreenDescriptorBlockParser.followingBlockParser();
                if (null != blockParserList) {
                    for (GifBlockParser blockParser : blockParserList) {
                        blockDataList.add(blockParser.readBlock(reader));
                    }
                }
                //Step3: process all the sub-blocks.
                readContent(blockDataList, reader);
            }
            long time = SystemClock.elapsedRealtime()-st;
            Log.i(TAG,"initialize time:"+time);
            //test initialize time:22
            Context context = getContext();
            GifDataBlockAdapter dataBlockAdapter = new GifDataBlockAdapter(context, imageFile, blockDataList);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(dataBlockAdapter);
        }
    }

    private void readExtension(List<GifBlockData> blockDataList, Reader reader) throws IOException {
        int separator = reader.readByte()&0xFF;
        switch (separator) {
            case 0xF9: {
                // 23. Graphic Control Extension.
                //i) Graphic Control Label - Identifies the current block as a Graphic Control Extension. This field contains the fixed value 0xF9.
//                Log.i(TAG,"Start read the Graphic Control Extension.");
                blockDataList.add(graphicsControlExtensionParser.readBlock(reader));
                break;
            }
            case 0xFE: {
                // 24. Comment Extension.
                //i) Comment Label - Identifies the block as a Comment Extension.This field contains the fixed value 0xFE.
//                Log.i(TAG,"Start read the Comment Extension.\n");
                blockDataList.add(commentExtensionBlockParser.readBlock(reader));
                break;
            }
            case 0x01: {
                // 25. Plain Text Extension.
                //i) Plain Text Label - Identifies the current block as a Plain Text Extension. This field contains the fixed value 0x01.
//                Log.i(TAG,"Start read the Plain Text Extension.\n");
                blockDataList.add(plainTextExtensionBlockParser.readBlock(reader));
                break;
            }
            case 0xFF: {
                // 26. Application Extension Label.
                //ii) Application Extension Label - Identifies the block as an Application Extension. This field contains the fixed value 0xFF.
//                Log.i(TAG,"Start read the Application Extension Label.\n");
                blockDataList.add(applicationExtensionBlockParser.readBlock(reader));
                break;
            }
        }
    }

    private void readContent(List<GifBlockData> blockDataList,Reader reader) throws IOException {
        int separator;
        while(true) {
            separator=reader.readByte() & 0xFF;
            switch (separator) {
                case 0x2C: {
//                    Log.i(TAG,"Start read the Image Separator.\n");
                    //i) Image Separator - Identifies the beginning of an Image Descriptor. This field contains the fixed value 0x2C.
                    blockDataList.add(imageDescriptorBlockParser.readBlock(reader));
                    List<GifBlockParser> blockParserList = imageDescriptorBlockParser.followingBlockParser();
                    if(null!=blockParserList){
                        for(GifBlockParser blockParser:blockParserList){
                            blockDataList.add(blockParser.readBlock(reader));
                        }
                    }
                    break;
                }
                case 0x21: {
//                    Log.i(TAG,"Start read the Extension.\n");
                    readExtension(blockDataList,reader);
                    break;
                }
                case 0x3B: {
                    // 27. Trailer.
                    //i)a. Description. This block is a single-field block indicating the end of the GIF Data Stream.  It contains the fixed value 0x3B.
//                    Log.i(TAG,"The Trailer. this is the end of the file.\n");
                    TerminatorBlockParser terminatorBlockParser = new TerminatorBlockParser();
                    blockDataList.add(terminatorBlockParser.readBlock(reader));
                    return;
                }
                case 0: // bad byte, but keep going and see what happens.
                    break;
                default:
//                    Log.i(TAG,"Unknown code:"+separator+"\n");
            }
        }
    }
}
