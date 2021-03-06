package com.zhhz.reader;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.StrictMode;
import android.provider.Settings;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;

import com.zhhz.reader.service.LogMonitorService;
import com.zhhz.reader.util.CrashHandler;

public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    //调试模式
    public static boolean DeBug = false;
    public static BitmapDrawable coverDrawable;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        MyApplication.coverDrawable = (BitmapDrawable) AppCompatResources.getDrawable(context, R.drawable.no_cover);
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(context);

        if (DeBug) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean bool = sharedPrefs.getBoolean("log",false);
        if (bool) {
            boolean isAllGranted = Settings.canDrawOverlays(context);
            if (isAllGranted){
                startService(new Intent(this, LogMonitorService.class));
            } else {
                sharedPrefs.edit().putBoolean("log",false).apply();
            }
        }

    }
}
