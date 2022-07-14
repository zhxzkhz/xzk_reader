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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.zhhz.reader.R;
import com.zhhz.reader.ui.bookrack.BookRackViewModel;
import com.zhhz.reader.util.LogUtil;

import java.io.File;
import java.util.ArrayList;

import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.file.Tailer;

/**
 * 监听 {@link com.zhhz.reader.util.LogUtil} 生成的日志，只从开启后生效
 */
public class LogMonitorService extends Service {

    private LogMonitorServiceViewModel model;
    private Observer<String> observer;
    private Tailer tailer;
    private MaterialCardView card;
    private ArrayList<String> list;
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
        model = new LogMonitorServiceViewModel();
        crateFloatWindow(this);
        tailer = new Tailer(new File(LogUtil.path), new LineHandler() {
            @Override
            public void handle(String line) {

            }
        });

        observer = s -> {
            list.add(s);
        };
        model.getData().observeForever(observer);

        tailer.start(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void crateFloatWindow(Context context) {

        // 添加一个悬浮窗，
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        card = new MaterialCardView(context);
        card.setBackgroundColor(0xffaaeecc);
        card.setCardBackgroundColor(0xffaaeecc);
        card.setRadius(24);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = -1;
        params.height = -1;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.gravity = Gravity.CENTER | Gravity.END;

        windowManager.addView(card, params);
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
            if (card.getX() > (width / 2f)) {
                card.setX(card.getWidth() / 2f);
            } else {
                card.setX(width - card.getWidth() / 2f);
            }
            card.setAlpha(0.35f);
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
                System.out.println("distanceX = " + distanceX);
                card.setX(card.getX() - distanceX);
                card.setY(card.getY() - distanceY);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                card.setVisibility(View.GONE);
                return super.onSingleTapUp(e);
            }
        });

        card.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

    }

    private void clearFloatWindow(){
        // 添加一个悬浮窗，
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.removeView(card);
    }

    @Override
    public void onDestroy() {
        model.getData().removeObserver(observer);
        clearFloatWindow();
        tailer.stop();
        super.onDestroy();
    }
}