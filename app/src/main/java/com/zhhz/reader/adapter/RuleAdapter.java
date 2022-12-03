package com.zhhz.reader.adapter;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zhhz.reader.bean.RuleBean;

import java.util.ArrayList;

/**
 * 规则适配器
 */

// ① 创建Adapter
public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.ViewHolder> {

    private final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
    private ArrayList<RuleBean> itemData;
    private View.OnClickListener onClickListener;

    private View.OnLongClickListener onLongClickListener;

    public RuleAdapter() {
        itemData = new ArrayList<>();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setItemData(ArrayList<RuleBean> mData) {
        this.itemData = mData;
    }

    public ArrayList<RuleBean> getItemData() {
        return itemData;
    }

    //③ 在Adapter中实现3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SwitchMaterial view = new SwitchMaterial(parent.getContext());
        view.setLayoutParams(layoutParams);
        view.setPadding(25, 20, 10, 20);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
        if (onLongClickListener != null) {
            view.setOnLongClickListener(onLongClickListener);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.switchMaterial.setText(itemData.get(position).getName());
        holder.switchMaterial.setChecked(itemData.get(position).isOpen());
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

        public final SwitchMaterial switchMaterial;

        private ViewHolder(SwitchMaterial v) {
            super(v);
            this.switchMaterial = v;
        }
    }


}






