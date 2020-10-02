package com.cz.android.gif.sample.visualize.extension;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cz.android.gif.GifDecoder;
import com.cz.android.gif.reader.Reader;
import com.cz.android.gif.sample.visualize.GifBlockParser;
import com.cz.android.gif.sample.visualize.view.GifBlockData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cz on 2020/9/19.
 * Process the extension of the file.
 * Please refer to the resources file: document/spec-gif89a
 */
public class ExtensionBlockParser extends GifBlockParser {
    private final List<GifBlockParser> blockParserList=new ArrayList<>();
    public ExtensionBlockParser() {
        blockParserList.add(new ApplicationExtensionBlockParser());
        blockParserList.add(new CommentExtensionBlockParser());
        blockParserList.add(new GraphicsControlExtensionParser());
        blockParserList.add(new PlainTextExtensionBlockParser());
    }
    @Override
    public void printBlockMessage() {
        Log.i(TAG,"Start parse Extensions.");
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
//        int separator = reader.readByte()&0xFF;
//        switch (separator){
//            case 0xF9:{
//                // 23. Graphic Control Extension.
//                //i) Graphic Control Label - Identifies the current block as a Graphic Control Extension. This field contains the fixed value 0xF9.
//                System.out.println("Start read the Graphic Control Extension.");
//                GraphicsControlExtensionParser graphicsControlExtensionParser = new GraphicsControlExtensionParser();
//                graphicsControlExtensionParser.onBindView(context,layout, blockData,byteArray);
//            }
//            case 0xFE:{
//                // 24. Comment Extension.
//                //i) Comment Label - Identifies the block as a Comment Extension.This field contains the fixed value 0xFE.
//                System.out.println("Start read the Comment Extension.\n");
//                CommentExtensionBlockParser commentExtensionBlockParser = new CommentExtensionBlockParser();
//                commentExtensionBlockParser.onBindView(context,layout, blockData,byteArray);
//            }
//            case 0x01:{
//                // 25. Plain Text Extension.
//                //i) Plain Text Label - Identifies the current block as a Plain Text Extension. This field contains the fixed value 0x01.
//                System.out.println("Start read the Plain Text Extension.\n");
//                PlainTextExtensionBlockParser plainTextExtensionBlockParser = new PlainTextExtensionBlockParser();
//                plainTextExtensionBlockParser.onBindView(context,layout, blockData,byteArray);
//            }
//            case 0xFF:{
//                // 26. Application Extension Label.
//                //ii) Application Extension Label - Identifies the block as an Application Extension. This field contains the fixed value 0xFF.
//                System.out.println("Start read the Application Extension Label.\n");
//                ApplicationExtensionBlockParser applicationExtensionBlockParser = new ApplicationExtensionBlockParser();
//                applicationExtensionBlockParser.onBindView(context,layout, blockData,byteArray);
//            }
//        }
    }

    @Override
    public boolean applyIdentifier(int b) {
        return 0x21==b;
    }
}
