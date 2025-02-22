package com.zhhz.reader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.zhhz.reader.R;
import com.zhhz.reader.util.GlideApp;
import com.zhhz.reader.util.LogUtil;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 漫画阅读适配器
 */

// ① 创建Adapter
public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ViewHolder> {

    private final Context context;
    private final FrameLayout.LayoutParams layout_params = new FrameLayout.LayoutParams(-1, -2);
    private final RequestListener<Bitmap> requestListener = new RequestListener<Bitmap>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Bitmap> target, boolean isFirstResource) {
            AppCompatTextView tv = ((FrameLayout) ((BitmapImageViewTarget) target).getView().getParent()).findViewById(R.id.item_title);
            tv.setText("加载失败\n点击重新加载");
            tv.setOnClickListener(v -> {
                Objects.requireNonNull(target.getRequest()).begin();
                tv.setText("加载中");
                tv.setOnClickListener(null);
            });
            LogUtil.error(e);
            return false;
        }

        @Override
        public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model, Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
            ((BitmapImageViewTarget) target).getView().setLayoutParams(layout_params);
            ((FrameLayout) ((BitmapImageViewTarget) target).getView().getParent()).setMinimumHeight(0);
            ((AppCompatTextView)((FrameLayout) ((BitmapImageViewTarget) target).getView().getParent()).findViewById(R.id.item_title)).setText(null);
            return false;
        }
    };
    private ArrayList<GlideUrl> itemData;

    public ComicAdapter(Context context) {
        this.context = context;
        this.itemData = new ArrayList<>();
        layout_params.gravity = Gravity.CENTER;
    }

    public ArrayList<GlideUrl> getItemData() {
        return itemData;
    }

    public void setItemData(ArrayList<GlideUrl> mData) {
        this.itemData = mData;
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comic_reader, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText("加载中");
        ((FrameLayout) holder.imageView.getParent()).setMinimumHeight(1080);
        GlideApp.with(context)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(requestListener)
                .load(itemData.get(position))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return itemData.size();
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public int getItemViewType(int type) {
        return 2;
    }

    //② 创建ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final AppCompatImageView imageView;
        public final AppCompatTextView title;

        private ViewHolder(View v) {
            super(v);
            this.imageView = v.findViewById(R.id.item_image);
            this.title = v.findViewById(R.id.item_title);
        }
    }


}






