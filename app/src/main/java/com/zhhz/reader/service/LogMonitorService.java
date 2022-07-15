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
 * 监听 {@link com.zhhz.reader.util.LogUtil} 生成的日志，只从开启后生效 <br>
 * 会创建一个悬浮球，点击后会显示日志
 */
public class LogMonitorService extends Service {

    private final ArrayList<String> list = new ArrayList<>();
    private ServiceFloatWindowBinding binding;
    private LogCatAdapter logCatAdapter;

    private Tailer tailer;

    private MaterialCardView card;

    public LogMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        setTheme(R.style.Theme_小说阅读器);
        super.onCreate();
        //开始创建悬浮按钮
        crateFloatWindow(this);
        //开始监听日志
        tailer = new Tailer(new File(LogUtil.path), line -> {
            //新增的日志添加到日志适配器并刷新
            LogMonitorService.this.list.add(line);
            logCatAdapter.notifyItemInserted(list.size());
        });
        //异步执行，同步会卡死
        tailer.start(true);
    }

    /**
     * 创建悬浮窗
     *
     * @param context 上下文
     */
    @SuppressLint("ClickableViewAccessibility")
    private void crateFloatWindow(Context context) {
        //获取系统悬浮窗口
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //实例化一个悬浮球按钮
        card = new MaterialCardView(context);
        card.setCardBackgroundColor(0xffaaeecc);
        card.setRadius(60);

        //悬浮按钮 LayoutParams
        WindowManager.LayoutParams params1 = new WindowManager.LayoutParams();
        params1.width = 120;
        params1.height = 120;
        params1.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params1.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params1.gravity = Gravity.START | Gravity.TOP;

        //日志适配器 LayoutParams
        WindowManager.LayoutParams params2 = new WindowManager.LayoutParams();
        params2.width = -1;
        params2.height = -2;
        params2.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params2.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params2.gravity = Gravity.BOTTOM;

        //悬浮按钮添加到屏幕上
        windowManager.addView(card, params1);

        //获取屏幕事件宽度
        int width;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            width = windowManager.getCurrentWindowMetrics().getBounds().width();
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
        }

        // 创建一个Handler，用于处理悬浮自动靠边和透明话事件
        Handler handler = new Handler();

        //悬浮球自动靠和透明边事件
        Runnable runnable = () -> {
            if (params1.x < (width / 2f)) {
                params1.x = 0;
                windowManager.updateViewLayout(card, params1);
                card.setX(card.getWidth() / -2f);
            } else {
                params1.x = width;
                windowManager.updateViewLayout(card, params1);
                card.setX(card.getWidth() / 2f);
            }
            card.setAlpha(0.3f);
        };

        //首次添加悬浮球到屏幕添加一个延迟悬浮靠边事件
        handler.postDelayed(runnable, 1500);

        //获取状态栏高度
        final int status_bar_height;
        @SuppressLint("InternalInsetResource") int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            status_bar_height = context.getResources().getDimensionPixelSize(resourceId);
        } else {
            status_bar_height = 0;
        }

        //设置点击和移动事件
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            float rx, ry, vx, vy, lx, ly;

            @Override
            public boolean onDown(MotionEvent e) {
                card.setX(0);
                card.setAlpha(1f);
                lx = rx = e.getRawX();
                ly = ry = e.getRawY();
                vx = rx - e.getX();
                vy = ry - e.getY();
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                rx = e2.getRawX();
                ry = e2.getRawY();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (handler.hasCallbacks(runnable))
                        handler.removeCallbacks(runnable);
                } else {
                    handler.removeCallbacks(runnable);
                }
                handler.postDelayed(runnable, 1500);
                params1.x = (int) (vx + (rx - lx));
                params1.y = (int) (vy + (ry - ly) - status_bar_height);
                windowManager.updateViewLayout(card, params1);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                card.setVisibility(View.GONE);
                windowManager.addView(binding.getRoot(), params2);
                handler.post(runnable);
                return super.onSingleTapUp(e);
            }
        });

        //悬浮球添加触摸事件
        card.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        //创建适配器视图
        binding = ServiceFloatWindowBinding.inflate((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        binding.getRoot().setBackgroundColor(0xffffffff);

        //创建适配器
        logCatAdapter = new LogCatAdapter(list);
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerView.setAdapter(logCatAdapter);

        //设置隐藏适配器点击事件
        binding.hide.setOnClickListener(v -> {
            windowManager.removeView(binding.getRoot());
            card.setVisibility(View.VISIBLE);
        });

        //设置清除日志点击事件
        binding.clear.setOnClickListener(v -> {
            int length = list.size();
            LogMonitorService.this.list.clear();
            logCatAdapter.notifyItemRangeChanged(0, length);
        });

    }

    //清除悬浮窗和适配器
    private void clearFloatWindow() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.removeView(card);
        windowManager.removeView(binding.getRoot());
    }

    @Override
    public void onDestroy() {
        list.clear();
        clearFloatWindow();
        tailer.stop();
        super.onDestroy();
    }
}