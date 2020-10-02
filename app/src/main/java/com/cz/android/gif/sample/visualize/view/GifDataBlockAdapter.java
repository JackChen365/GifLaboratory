package com.cz.android.gif.sample.visualize.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cz.android.gif.GifDecoder;
import com.cz.android.gif.reader.BufferedChannelReader;
import com.cz.android.gif.sample.visualize.ColorTableParser;
import com.cz.android.gif.sample.visualize.GifBlockParser;
import com.cz.android.gif.sample.visualize.HeaderBlockParser;
import com.cz.android.gif.sample.visualize.ImageDataBlockParser;
import com.cz.android.gif.sample.visualize.ImageDescriptorBlockParser;
import com.cz.android.gif.sample.visualize.LogicalScreenDescriptorBlockParser;
import com.cz.android.gif.sample.visualize.TerminatorBlockParser;
import com.cz.android.gif.sample.visualize.extension.ApplicationExtensionBlockParser;
import com.cz.android.gif.sample.visualize.extension.CommentExtensionBlockParser;
import com.cz.android.gif.sample.visualize.extension.GraphicsControlExtensionParser;
import com.cz.android.gif.sample.visualize.extension.PlainTextExtensionBlockParser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class GifDataBlockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG="GifDataBlockAdapter";
    private final SparseArray<GifBlockParser> blockParserSparseArray=new SparseArray<>();
    private final List<GifBlockData> blockDataList=new ArrayList<>();
    private Context context;
    private final LayoutInflater layoutInflater;
    private final File file;
    private VisualizeGifDecoder decoder;
    private ByteBuffer byteBuffer;

    public GifDataBlockAdapter(Context context,File file, List<GifBlockData> blockDataList) {
        this.context=context;
        this.file = file;
        this.layoutInflater=LayoutInflater.from(context);
        decoder = new VisualizeGifDecoder();
        try {
            decoder.loadFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(null!=blockDataList){
            this.blockDataList.addAll(blockDataList);
        }
        blockParserSparseArray.put(GifBlockParser.HEADER,new HeaderBlockParser());
        blockParserSparseArray.put(GifBlockParser.LOGICAL_SCREEN_DESCRIPTOR,new LogicalScreenDescriptorBlockParser());
        blockParserSparseArray.put(GifBlockParser.IMAGE_INSPECTOR,new ImageDescriptorBlockParser());
        blockParserSparseArray.put(GifBlockParser.IMAGE_DATA_BLOCK,new ImageDataBlockParser());
        blockParserSparseArray.put(GifBlockParser.COLOR_TABLE_EXTENSION,new ColorTableParser());
        blockParserSparseArray.put(GifBlockParser.APPLICATION_EXTENSION,new ApplicationExtensionBlockParser());
        blockParserSparseArray.put(GifBlockParser.COMMENT_EXTENSION,new CommentExtensionBlockParser());
        blockParserSparseArray.put(GifBlockParser.GRAPHICS_CONTROL_EXTENSION,new GraphicsControlExtensionParser());
        blockParserSparseArray.put(GifBlockParser.PLAIN_TEXT_EXTENSION,new PlainTextExtensionBlockParser());
        blockParserSparseArray.put(GifBlockParser.TERMINATOR,new TerminatorBlockParser());
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        decoder.bitmapSparseArray.clear();
        decoder.close();
    }

    @Override
    public int getItemCount() {
        return blockDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        GifBlockData blockData = blockDataList.get(position);
        return blockData.type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GifBlockParser blockParser = blockParserSparseArray.get(viewType);
        View layout=blockParser.onCreateView(context,layoutInflater,parent);
        return new RecyclerView.ViewHolder(layout) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GifBlockData blockData = blockDataList.get(position);
        try {
            if(null==byteBuffer||byteBuffer.capacity()<(blockData.end-blockData.start)){
                byteBuffer=ByteBuffer.allocate((int) (blockData.end-blockData.start));
            }
            BufferedChannelReader bufferedReader = decoder.getBufferedReader();
            FileChannel channel = bufferedReader.getFileChannel();
            channel.position(blockData.start);
            byteBuffer.clear();
            byteBuffer.limit((int) (blockData.end-blockData.start));
            channel.read(byteBuffer);
            byteBuffer.flip();

            int itemViewType = getItemViewType(position);
            GifBlockParser blockParser = blockParserSparseArray.get(itemViewType);
            //Initialize the image block data.
            if(blockData instanceof GifColorBlockData){
                GifColorBlockData imageBlockData = (GifColorBlockData) blockData;
                ColorTableParser colorTableParser = (ColorTableParser) blockParser;
                colorTableParser.setTitle(imageBlockData.title);
                colorTableParser.setColorTableSize(imageBlockData.colorTableSize);
            }
            blockData.file = file;
            blockParser.onBindView(context, decoder,holder.itemView,blockData,byteBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class VisualizeGifDecoder extends GifDecoder {

        SparseArray<Bitmap> bitmapSparseArray=new SparseArray<>();

        @Nullable
        @Override
        public Bitmap decodeFrame(int index) {
            Bitmap bitmap = bitmapSparseArray.get(index);
            if(null==bitmap){
                bitmap = super.decodeFrame(index);
                if(null!=bitmap){
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    bitmapSparseArray.put(index,bitmap);
                }
            }
            return bitmap;
        }
    }
}
