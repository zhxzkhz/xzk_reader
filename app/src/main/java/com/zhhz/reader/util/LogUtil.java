package com.zhhz.reader.util;

import android.util.Log;

import com.zhhz.reader.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 日志工具，主要用于js日志，输出失败时使用Log.i进行输出
 */
public class LogUtil {
    public static String path = null;

    private static BufferedWriter bufferedWriter = null;

    static {
        try {
            path = MyApplication.context.getExternalCacheDir().getAbsolutePath() + File.separator + "log";
            if (!new File(path).isDirectory()){
                if (!new File(path).mkdirs()) {
                    throw new FileNotFoundException();
                }
            }
            path = path + File.separator + System.currentTimeMillis() + File.separator + ".log";
            bufferedWriter = new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean check(Object o,String s){
        if (bufferedWriter == null){
            Log.i(s,String.valueOf(o));
            return true;
        }
        return false;
    }

    public void error(Object object){
        if (check(object,"error")) return;
        try {
            if (object instanceof Throwable){
                bufferedWriter.append("error : ").write(((Throwable) object).getMessage());
            }else {
                bufferedWriter.append("error : ").write(String.valueOf(object));
            }
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            Log.i("error",String.valueOf(object));
        }
    }

    public void info(Object object){
        if (check(object,"info")) return;
        try {
            bufferedWriter.append("info : ").write(String.valueOf(object));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            Log.i("info",String.valueOf(object));
        }
    }

    public void warning(Object object){
        if (check(object,"warning")) return;
        try {
            bufferedWriter.append("warning : ").write(String.valueOf(object));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            Log.i("warning",String.valueOf(object));
        }
    }

}
