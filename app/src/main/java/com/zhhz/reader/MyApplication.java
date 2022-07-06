package com.zhhz.reader;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.zhhz.reader.util.CrashHandler;

public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    //调试模式
    public static boolean DeBug = true;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(context);

        if (DeBug) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }

    }
}
