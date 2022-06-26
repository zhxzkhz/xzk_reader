package com.zhhz.reader.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhhz.reader.R;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.GlideApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


// ① 创建Adapter
public class CatalogueAdapter extends RecyclerView.Adapter<CatalogueAdapter.ViewHolder> {

    private LinkedHashMap<String, String> itemData;

    private final ArrayList<String> title;
    private final ArrayList<String> url;

    private final Context context;

    private View.OnClickListener onClickListener;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setItemData(LinkedHashMap<String, String> mData) {
        this.itemData = mData;
        for (Map.Entry<String, String> entry : mData.entrySet()) {
            title.add(entry.getKey());
            url.add(entry.getValue());
        }
    }

    public LinkedHashMap<String, String> getItemData() {
        return itemData;
    }

    //② 创建ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public AppCompatTextView textView;

        private ViewHolder(AppCompatTextView v) {
            super(v);
            this.textView = v;
        }
    }

    public CatalogueAdapter(Context context) {
        this.context = context;
        itemData = new LinkedHashMap<>();
        title = new ArrayList<>();
        url = new ArrayList<>();
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppCompatTextView view = new AppCompatTextView(parent.getContext());
        view.setPadding(25,15,10,15);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(title.get(position));
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


}






