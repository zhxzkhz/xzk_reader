package com.zhhz.reader.adapter;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * 选择文本菜单适配器
 */

// ① 创建Adapter
public class SelectTextAdapter extends RecyclerView.Adapter<SelectTextAdapter.ViewHolder> {

    private ArrayList<? extends MenuItem> menu;
    private final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);

    private int pos = -1;
    private View.OnClickListener onClickListener;

    public SelectTextAdapter() {
        menu = new ArrayList<>();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setItemData(ArrayList<? extends MenuItem> mData) {
        this.menu = mData;
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppCompatTextView view = new AppCompatTextView(parent.getContext());
        view.setLayoutParams(layoutParams);
        view.setPadding(5, 5, 5, 5);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
        return new ViewHolder(view);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(menu.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return menu.size();
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






