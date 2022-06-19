package com.zhhz.reader.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class XluaPreloadModelProvider implements ListPreloader.PreloadModelProvider<String> {
    private final Context context;
    private final List<Object> urls;

    public XluaPreloadModelProvider(Context context, List<Object> urls) {
        this.context = context;
        this.urls = urls;
    }

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        String url = (String) urls.get(position);
        if (TextUtils.isEmpty(url)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(url);
    }


    @Nullable
    @Override
    public RequestBuilder<Bitmap> getPreloadRequestBuilder(@NonNull String item) {
        //返回的 RequestBuilder ，必须与你从 onBindViewHolder 里启动的请求使用完全相同的一组选项 (占位符， 变换等) 和完全相同的尺寸
        return GlideApp.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESOURCE).load(item);
    }



}
