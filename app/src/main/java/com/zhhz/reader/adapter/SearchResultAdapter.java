package com.zhhz.reader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhhz.reader.MyApplication;
import com.zhhz.reader.R;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.GlideApp;

import java.util.ArrayList;


// ① 创建Adapter
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<SearchResultBean> itemData;
    private View.OnClickListener onClickListener;

    public SearchResultAdapter(Context context) {
        this.context = context;
        this.itemData = new ArrayList<>();
    }

    public SearchResultAdapter(Context context, ArrayList<SearchResultBean> data) {
        this.context = context;
        this.itemData = data;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public ArrayList<SearchResultBean> getItemData() {
        return itemData;
    }

    public void setItemData(ArrayList<SearchResultBean> mData) {
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
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultBean book = itemData.get(position);
        holder.title.setText(book.getTitle());
        if (holder.author != null) {
            if (book.getAuthor() != null) {
                holder.author.setText(book.getAuthor());
            } else {
                holder.author.setText(null);
            }
        }
        if (book.getCover() != null) {
            GlideApp.with(context)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .error(MyApplication.coverDrawable)
                    .centerCrop()
                    .load(book.getCover())
                    .into(holder.imageView);
        } else {
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
        public final AppCompatImageView imageView;
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






