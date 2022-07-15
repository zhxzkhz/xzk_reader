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
import java.util.ArrayList;
import java.util.Map;


// ① 创建Adapter
public class LogCatAdapter extends RecyclerView.Adapter<LogCatAdapter.ViewHolder> {

    private final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
    
    private final ArrayList<String> itemData;
    private View.OnClickListener onClickListener;

    public LogCatAdapter(ArrayList<String> list) {
        itemData = list;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
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
        String s = itemData.get(position);
        holder.textView.setText(s);
        if (s.startsWith("error :")) {
            holder.textView.setTextColor(Color.RED);
        } else if (s.startsWith("warning :")){
            holder.textView.setTextColor(Color.YELLOW);
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






