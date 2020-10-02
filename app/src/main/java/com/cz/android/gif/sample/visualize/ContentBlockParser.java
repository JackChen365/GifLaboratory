package com.cz.android.gif.sample.visualize;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cz.android.gif.GifDecoder;
import com.cz.android.gif.reader.Reader;
import com.cz.android.gif.sample.visualize.extension.ExtensionBlockParser;
import com.cz.android.gif.sample.visualize.view.GifBlockData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cz on 2020/9/19.
 * Process the content of the file.
 * Please refer to the resources file: document/spec-gif89a
 */
public class ContentBlockParser extends GifBlockParser {
    private static final String TAG="ContentBlockParser";
    private final List<GifBlockParser> blockParserList=new ArrayList<>();

    public ContentBlockParser() {
        blockParserList.add(new ImageDescriptorBlockParser());
        blockParserList.add(new ExtensionBlockParser());
        blockParserList.add(new TerminatorBlockParser());
    }

    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse the ContentExtension.");
    }

    @Override
    public GifBlockData readBlock(Reader reader) throws IOException {
        return null;
    }

    @Override
    public View onCreateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindView(Context context, GifDecoder decoder, View layout, GifBlockData blockData, ByteBuffer byteBuffer) throws IOException {
//        boolean handledBlock=false;
//        while(true) {
//            int identifies=reader.readByte() & 0xFF;
//            if(0 == identifies){
//                // bad byte, but keep going and see what happens.
//                // Actually this is some code that I forgot to skip.
//                continue;
//            }
//            for(GifBlockParser blockParser:blockParserList){
//                if(blockParser.applyIdentifier(identifies)){
//                    handledBlock = true;
//                    blockParser.process(context,container,reader);
//                    break;
//                }
//            }
//            if(!handledBlock){
//                System.out.println("Unknown code:"+identifies+"\n");
//                break;
//            }
//        }
//        int separator;
//        while(true) {
//            separator=reader.readByte() & 0xFF;
//            switch (separator) {
//                case 0x2C: {
//                    //i) Image Separator - Identifies the beginning of an Image Descriptor. This field contains the fixed value 0x2C.
//                    Log.i(TAG,"Start read the Image Separator.\n");
//                    ImageDescriptorBlockParser imageDescriptorBlockParser = new ImageDescriptorBlockParser();
//                    return imageDescriptorBlockParser.onBindView(context,reader);
//                }
//                case 0x21: {
//                    Log.i(TAG,"Start read the Extension.\n");
//                    ExtensionBlockParser extensionBlockParser = new ExtensionBlockParser();
//                    return extensionBlockParser.onBindView(context,reader);
//                }
//                case 0x3B: {
//                    // 27. Trailer.
//                    //i)a. Description. This block is a single-field block indicating the end of the GIF Data Stream.  It contains the fixed value 0x3B.
//                    Log.i(TAG,"The Trailer. this is the end of the file.\n");
//                    TerminatorBlockParser terminatorBlockParser = new TerminatorBlockParser();
//                    return terminatorBlockParser.onBindView(context,reader);
//                }
//                case 0: // bad byte, but keep going and see what happens.
//                    break;
//                default:
//                    Log.i(TAG,"Unknown code:"+separator+"\n");
//                    return null;
//            }
//        }
    }

    @Override
    public boolean applyIdentifier(int b) {
        return true;
    }
}
