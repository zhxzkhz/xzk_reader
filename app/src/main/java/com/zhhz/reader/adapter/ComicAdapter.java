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
import androidx.appcompat.widget.AppCompatButton;
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
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.GlideApp;
import com.zhhz.reader.util.GlideRequest;

import java.util.ArrayList;
import java.util.Objects;


// ① 创建Adapter
public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ViewHolder> {

    private ArrayList<GlideUrl> itemData;

    private final Context context;

    private View.OnClickListener onClickListener;

    private final FrameLayout.LayoutParams layout_params = new FrameLayout.LayoutParams(-1, -2);

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setItemData(ArrayList<GlideUrl> mData) {
        this.itemData = mData;
    }

    public ArrayList<GlideUrl> getItemData() {
        return itemData;
    }

    //② 创建ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView imageView;
        public AppCompatTextView title;

        private ViewHolder(View v) {
            super(v);
            this.imageView = v.findViewById(R.id.item_image);
            this.title = v.findViewById(R.id.item_title);
        }
    }

    public ComicAdapter(Context context) {
        this.context = context;
        this.itemData = new ArrayList<>();
        layout_params.gravity = Gravity.CENTER;
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comic_reader, parent, false);
        return new ViewHolder(view);
    }


    private final RequestListener<Bitmap> requestListener = new RequestListener<Bitmap>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            AppCompatButton button = new AppCompatButton(context);
            button.setText("重新加载");
            button.setOnClickListener(v -> {
                Objects.requireNonNull(target.getRequest()).begin();
                ((FrameLayout)v.getParent()).removeView(v);
            });
            ((FrameLayout)((BitmapImageViewTarget) target).getView().getParent()).addView(button,layout_params);
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            ((BitmapImageViewTarget) target).getView().setLayoutParams(layout_params);
            ((FrameLayout)((BitmapImageViewTarget) target).getView().getParent()).setMinimumHeight(0);
            return false;
        }
    };

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //String book = itemData.get(position);
        //holder.title.setText(book.getTitle());
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


}






