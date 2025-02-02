package com.zhhz.reader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.zhhz.reader.MyApplication;
import com.zhhz.reader.R;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.util.GlideApp;

import java.util.ArrayList;

/**
 * 书架适配器
 */

// ① 创建Adapter
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<BookBean> itemData;
    private View.OnClickListener onClickListener;
    private SelectionTracker<String> mSelectionTracker;

    public BookAdapter(Context context) {
        this.context = context;
        this.itemData = new ArrayList<>();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public ArrayList<BookBean> getItemData() {
        return itemData;
    }

    public void setItemData(ArrayList<BookBean> mData) {
        this.itemData = mData;
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_grid_item_layout, parent, false);
        } else {
            view = new View(parent.getContext());
        }
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookBean book = itemData.get(position);
        if (mSelectionTracker != null) {
            boolean bool = mSelectionTracker.isSelected(book.getBookId());
            if (bool) {
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
            holder.checkBox.setChecked(bool);
        }

        holder.title.setText(book.getTitle());
        if (holder.author != null && book.getAuthor() != null) {
            holder.author.setText(book.getAuthor());
        }
        if (holder.last != null && book.getAuthor() != null) {
            holder.last.setText(book.getLastChapter());
        }
        if (book.getCover() != null) {
            long startTime = SystemClock.elapsedRealtime();
            GlideApp.with(context)
                    .asBitmap()
                    .load(book.getCover())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Bitmap> target, boolean isFirstResource) {
                            long endTime = SystemClock.elapsedRealtime();
                            Log.d("GlideLoadTime", "加载失败或者图片未能加载成功，用时：" + (endTime - startTime) + "ms");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model, Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            long endTime = SystemClock.elapsedRealtime();
                            Log.d("GlideLoadTime", "图片加载成功，用时：" + (endTime - startTime) + "ms");
                            return false;
                        }
                    })
                    .into(holder.bookCoverImageView);
        } else {
            holder.bookCoverImageView.setImageDrawable(MyApplication.coverDrawable);
        }
        if (holder.update != null) {
            if (book.getUpdate()) {
                holder.update.setVisibility(View.VISIBLE);
            } else {
                holder.update.setVisibility(View.GONE);
            }
        }
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
        return 1;
    }

    public void setSelectionTracker(SelectionTracker<String> mSelectionTracker) {
        this.mSelectionTracker = mSelectionTracker;
    }

    static class StringItemDetails extends ItemDetailsLookup.ItemDetails<String> {

        private final int position;
        private final String item;

        public StringItemDetails(int position, String item) {
            this.position = position;
            this.item = item;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Nullable
        @Override
        public String getSelectionKey() {
            return item;
        }
    }

    //② 创建ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final AppCompatImageView bookCoverImageView;
        public final AppCompatTextView title;
        public final AppCompatTextView author;
        public final AppCompatTextView last;
        public final AppCompatCheckBox checkBox;
        public final View update;

        private ViewHolder(View v) {
            super(v);
            this.bookCoverImageView = v.findViewById(R.id.item_image);
            this.title = v.findViewById(R.id.item_title);
            this.author = v.findViewById(R.id.item_author);
            this.last = v.findViewById(R.id.item_latest);
            this.checkBox = v.findViewById(R.id.item_check);
            this.update = v.findViewById(R.id.item_update);
        }

        public ItemDetailsLookup.ItemDetails<String> getItemDetails() {
            return new StringItemDetails(getBindingAdapterPosition(), itemData.get(getBindingAdapterPosition()).getBookId());
        }
    }

}






