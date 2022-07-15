package com.zhhz.reader.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.card.MaterialCardView;
import com.zhhz.reader.R;
import com.zhhz.reader.adapter.LogCatAdapter;
import com.zhhz.reader.databinding.ServiceFloatWindowBinding;
import com.zhhz.reader.util.LogUtil;

import java.io.File;
import java.util.ArrayList;

import cn.hutool.core.io.file.Tailer;

/**
 * 监听 {@link com.zhhz.reader.util.LogUtil} 生成的日志，只从开启后生效
 */
public class LogMonitorService extends Service {

    private ServiceFloatWindowBinding binding;
    private ArrayList<String> list;
    private LogCatAdapter logCatAdapter;
    private Tailer tailer;
    private MaterialCardView card;

    public LogMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        setTheme(R.style.Theme_小说阅读器);
        super.onCreate();
        crateFloatWindow(this);
        tailer = new Tailer(new File(LogUtil.path), line -> {
            LogMonitorService.this.list.add(line);
            logCatAdapter.notifyItemInserted(list.size());
        });
        //异步执行，同步会卡死
        tailer.start(true);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void crateFloatWindow(Context context) {

        // 添加一个悬浮窗，
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        card = new MaterialCardView(context);
        card.setBackgroundColor(0xffaaeecc);
        card.setCardBackgroundColor(0xffaaeecc);
        card.setRadius(36);

        //悬浮按钮 LayoutParams
        WindowManager.LayoutParams params1 = new WindowManager.LayoutParams();
        params1.width = 72;
        params1.height = 72;
        params1.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params1.gravity = Gravity.CENTER | Gravity.END;

        //日志适配器 LayoutParams
        WindowManager.LayoutParams params2 = new WindowManager.LayoutParams();
        params2.width = -1;
        params2.height = -2;
        params2.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params2.gravity = Gravity.END;

        windowManager.addView(card, params1);
        int width;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            width = windowManager.getCurrentWindowMetrics().getBounds().width();
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
        }
        Handler handler = new Handler();

        Runnable runnable = () -> {
            if (params1.x < (width / 2f)) {
                params1.x = (int) (card.getWidth() / 2f);
            } else {
                params1.x =width - (int) (card.getWidth() / 2f);
            }
            card.setAlpha(0.35f);
            windowManager.updateViewLayout(card,params1);
        };

        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                card.setAlpha(1f);
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (handler.hasCallbacks(runnable))
                        handler.removeCallbacks(runnable);
                } else {
                    handler.removeCallbacks(runnable);
                }
                handler.postDelayed(runnable, 3000);
                params1.x = (int) (params1.x - distanceX);
                params1.y = (int) (params1.y - distanceY);
                windowManager.updateViewLayout(card,params1);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                card.setVisibility(View.GONE);
                windowManager.addView(binding.getRoot(),params2);
                return super.onSingleTapUp(e);
            }
        });

        card.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        logCatAdapter = new LogCatAdapter(list);

        binding = ServiceFloatWindowBinding.inflate((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE));

        //设置Item增加、移除动画
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerView.setAdapter(logCatAdapter);

        binding.hide.setOnClickListener(v -> {
            windowManager.removeView(binding.getRoot());
            card.setVisibility(View.VISIBLE);
        });

        binding.clear.setOnClickListener(v -> {
            int length = list.size();
            LogMonitorService.this.list.clear();
            logCatAdapter.notifyItemRangeChanged(0,length);
        });

    }

    private void clearFloatWindow(){
        // 添加一个悬浮窗，
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.removeView(card);
    }

    @Override
    public void onDestroy() {
        list.clear();
        clearFloatWindow();
        tailer.stop();
        super.onDestroy();
    }
}