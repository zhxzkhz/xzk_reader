package com.zhhz.reader.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 目录适配器
 */

// ① 创建Adapter
public class CatalogueAdapter extends RecyclerView.Adapter<CatalogueAdapter.ViewHolder> {

    private final ArrayList<String> title;
    private final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
    //private final ArrayList<String> url;
    private LinkedHashMap<String, String> itemData;
    private int pos = -1;
    private View.OnClickListener onClickListener;

    public CatalogueAdapter() {
        itemData = new LinkedHashMap<>();
        title = new ArrayList<>();
        //url = new ArrayList<>();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setItemData(LinkedHashMap<String, String> mData) {
        this.itemData = mData;
        for (Map.Entry<String, String> entry : mData.entrySet()) {
            title.add(entry.getKey());
            //url.add(entry.getValue());
        }
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public ArrayList<String> getTitle() {
        return title;
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppCompatTextView view = new AppCompatTextView(parent.getContext());
        view.setLayoutParams(layoutParams);
        view.setPadding(25, 20, 10, 20);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(title.get(position));
        if (position == pos) {
            holder.textView.setTextColor(Color.BLUE);
        } else {
            holder.textView.setTextColor(Color.BLACK);
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

    //② 创建ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public AppCompatTextView textView;

        private ViewHolder(AppCompatTextView v) {
            super(v);
            this.textView = v;
        }
    }


}






