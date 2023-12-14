package com.zhhz.reader.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.zhhz.reader.util.OrderlyMap;

import java.util.ArrayList;

/**
 * 目录适配器
 */

// ① 创建Adapter
public class CatalogueAdapter extends RecyclerView.Adapter<CatalogueAdapter.ViewHolder> {

    private final ArrayList<String> titleList;
    private final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
    //private final ArrayList<String> url;
    private OrderlyMap itemData;
    private int pos = -1;
    private View.OnClickListener onClickListener;

    public CatalogueAdapter() {
        itemData = new OrderlyMap();
        titleList = new ArrayList<>();
        //url = new ArrayList<>();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setItemData(OrderlyMap mData) {
        this.itemData = mData;
        //url.add(entry.getValue());
        titleList.addAll(mData.keySet());
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public ArrayList<String> getTitleList() {
        return titleList;
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
        holder.textView.setText(titleList.get(position));
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

        public final AppCompatTextView textView;

        private ViewHolder(AppCompatTextView v) {
            super(v);
            this.textView = v;
        }
    }


}






