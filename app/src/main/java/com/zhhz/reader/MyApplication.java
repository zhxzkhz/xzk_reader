package com.zhhz.reader;

import static com.alibaba.fastjson2.util.BeanUtils.arrayOf;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.zhhz.reader.rule.Analysis;
import com.zhhz.reader.service.LogMonitorService;
import com.zhhz.reader.util.CrashHandler;
import com.zhhz.reader.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    //调试模式
    public static final boolean DeBug = false;
    public static BitmapDrawable coverDrawable;

    static {
        //设置刷新头
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            layout.setPrimaryColorsId(android.R.color.white, android.R.color.black);
            return new ClassicsHeader(context).setTimeFormat(new SimpleDateFormat("更新于 HH:mm:ss", Locale.CHINESE));
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        MyApplication.coverDrawable = (BitmapDrawable) AppCompatResources.getDrawable(context, R.drawable.no_cover);
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(context);

        if (DeBug) {
            //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            //StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    //.detectDiskReads()
                    //.detectDiskWrites()
                    .detectNetwork()   // 也可以加入网络检测
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
        }

        CompletableFuture.runAsync(() -> {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //是否开启日志悬浮窗
            boolean bool = sharedPrefs.getBoolean("log", false);
            if (bool) {
                //检测是否具有悬浮窗权限
                boolean isAllGranted = Settings.canDrawOverlays(context);
                if (isAllGranted) {
                    startService(new Intent(this, LogMonitorService.class));
                } else {
                    sharedPrefs.edit().putBoolean("log", false).apply();
                }
            }
        }).join();

        Analysis.setLogError(LogUtil::error);


    }
}
