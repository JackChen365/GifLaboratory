package com.cz.android.gif.sample.ui.test;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cz.android.gif.sample.R;
import com.cz.android.gif.sample.ndk.NativeTextureGifView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GifListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG="GifListAdapter";
    private List<String> errorList=new ArrayList<>();
    private OkHttpClient httpClient = new OkHttpClient();
    private List<RecyclerView.ViewHolder> pendingViewHolderList=new ArrayList<>();
    private RecyclerView recyclerView;
    private LayoutInflater layoutInflater;
    private List<String> imageList;
    private File dataDir;

    public GifListAdapter(@NonNull Context context,@NonNull List<String> imageList) throws IOException {
        layoutInflater = LayoutInflater.from(context);
        dataDir = context.getFilesDir();
        if(null!=imageList){
            this.imageList = new ArrayList<>();
            this.imageList.addAll(imageList);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView=recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        httpClient.dispatcher().cancelAll();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        TextView messageText=holder.itemView.findViewById(R.id.messageText);
        NativeTextureGifView textureGifView=holder.itemView.findViewById(R.id.textureGifView);
        int position = holder.getLayoutPosition();
        String url = imageList.get(position);
        File file = new File(dataDir, String.valueOf(url.hashCode()));
        if(file.exists()){
            messageText.setVisibility(View.GONE);
            Log.i(TAG,"onViewAttachedToWindow:"+position+" id:"+textureGifView.hashCode()+" start.");
            textureGifView.loadImage(file);
            textureGifView.start();
        }
        checkAndRemovePendingViewHolderList();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        int position = holder.getLayoutPosition();
        NativeTextureGifView textureGifView=holder.itemView.findViewById(R.id.textureGifView);
        Log.i(TAG,"onViewDetachedFromWindow:"+position+" id:"+textureGifView.hashCode()+" start.");
        if(!textureGifView.isLockAvailable()){
            holder.setIsRecyclable(false);
            pendingViewHolderList.add(holder);
        }
        checkAndRemovePendingViewHolderList();
    }

    private void checkAndRemovePendingViewHolderList(){
        Iterator<RecyclerView.ViewHolder> iterator = pendingViewHolderList.iterator();
        while(iterator.hasNext()){
            RecyclerView.ViewHolder holder = iterator.next();
            NativeTextureGifView textureGifView=holder.itemView.findViewById(R.id.textureGifView);
            if(textureGifView.isLockAvailable()){
                holder.setIsRecyclable(true);
                iterator.remove();
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.gif_image_layout, parent, false);
        return new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        TextView textView=holder.itemView.findViewById(R.id.textView);
        TextView messageText=holder.itemView.findViewById(R.id.messageText);
        textView.setText(String.valueOf(position));
        final String url = imageList.get(position);
        File file = new File(dataDir, String.valueOf(url.hashCode()));
        if(file.exists()){
            messageText.setVisibility(View.GONE);
        } else if(!errorList.contains(url)){
            messageText.setVisibility(View.GONE);
            downloadFile(position, url);
        } else {
            messageText.setVisibility(View.VISIBLE);
        }
    }

    private void downloadFile(final int position, String url) {
        Request request = new Request.Builder().tag(position).url(url).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if(200==response.code()){
                    HttpUrl httpUrl = response.request().url();
                    final File file = new File(dataDir, String.valueOf(httpUrl.url().toString().hashCode()));
                    saveImageFile(response,file);
                } else {
                    HttpUrl httpUrl = response.request().url();
                    errorList.add(httpUrl.toString());
                    Log.e(TAG,"url:"+httpUrl+" not found!");
                }
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemChanged(position);
                    }
                });
            }
        });
    }

    private void saveImageFile(@NotNull Response response,File file) {
        try {
            byte[] body = response.body().bytes();
            if(null!=body&&0 <body.length){
                FileOutputStream fileInputStream=new FileOutputStream(file);
                fileInputStream.write(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return imageList.size();
    }
}
