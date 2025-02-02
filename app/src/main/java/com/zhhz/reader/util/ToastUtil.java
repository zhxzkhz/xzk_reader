package com.zhhz.reader.util;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.zhhz.reader.MyApplication;


public class ToastUtil {
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void show(String text){
        show(text,Toast.LENGTH_SHORT);
    }

    public static void show(String text,int duration){
        handler.post(() -> Toast.makeText(MyApplication.context, text, duration).show());
    }

}
