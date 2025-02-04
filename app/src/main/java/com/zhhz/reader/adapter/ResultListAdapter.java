package com.zhhz.reader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.zhhz.reader.MyApplication;
import com.zhhz.reader.R;
import com.zhhz.reader.bean.ResultListBean;
import com.zhhz.reader.util.GlideApp;
import com.zhhz.reader.view.CoverImageView;

import java.util.ArrayList;

// ① 创建Adapter
public class ResultListAdapter extends RecyclerView.Adapter<ResultListAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<ResultListBean> itemData;
    private View.OnClickListener clickListener;

    public ResultListAdapter(Context context) {
        this.context = context;
        this.itemData = new ArrayList<>();
    }

    public void setOnClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ArrayList<ResultListBean> getItemData() {
        return itemData;
    }

    public void setItemData(ArrayList<ResultListBean> mData) {
        this.itemData = mData;
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_grid_item_layout, parent, false);
        } else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_linear_item_layout, parent, false);
        } else {
            view = new View(parent.getContext());
        }
        if (clickListener != null) {
            view.setOnClickListener(clickListener);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResultListBean book = itemData.get(position);
        holder.title.setText(book.getTitle());
        if (book.getAuthor() != null) {
            holder.author.setText(book.getAuthor());
        } else {
            holder.author.setText(null);
        }
        if (book.getLastChapter() != null) {
            holder.last.setText(book.getLastChapter());
        } else {
            holder.last.setText(null);
        }

        if (book.getCover() != null) {
            GlideApp.with(context)
                    .asBitmap()
                    .load(book.getCover())
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(MyApplication.coverDrawable)
                    .error(MyApplication.coverDrawable)
                    .centerCrop()
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Bitmap> target, boolean isFirstResource) {
                            holder.imageView.setNameAuthor(book.getTitle(), book.getAuthor());
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model, Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imageView);
        } else {
            holder.imageView.setNameAuthor(book.getTitle(), book.getAuthor());
            holder.imageView.setImageDrawable(MyApplication.coverDrawable);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        //回收时清空图片，防止被错乱
        GlideApp.with(context).clear(holder.imageView);
        super.onViewRecycled(holder);
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
        public final CoverImageView imageView;
        public final AppCompatTextView title;
        public final AppCompatTextView author;
        public final AppCompatTextView last;

        private ViewHolder(View v) {
            super(v);
            this.imageView = v.findViewById(R.id.item_image);
            this.title = v.findViewById(R.id.item_title);
            this.author = v.findViewById(R.id.item_author);
            this.last = v.findViewById(R.id.item_latest);
        }
    }


}






