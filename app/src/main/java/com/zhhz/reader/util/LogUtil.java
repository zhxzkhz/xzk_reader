package com.zhhz.reader.util;

import static com.zhhz.reader.util.DiskCache.SCRIPT_ENGINE;

import android.util.Log;

import com.zhhz.reader.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 日志工具，主要用于js日志，输出失败时使用Log.i进行输出
 */
public class LogUtil {
    public static String path = null;

    private static BufferedWriter bufferedWriter = null;
    private static PrintWriter printWriter = null;

    static {
        try {
            path = Objects.requireNonNull(MyApplication.context.getExternalCacheDir()).getAbsolutePath() + File.separator + "log";
            if (!new File(path).isDirectory()) {
                if (!new File(path).mkdirs()) {
                    throw new FileNotFoundException();
                }
            }
            path = path + File.separator + System.currentTimeMillis() + ".log";
            bufferedWriter = new BufferedWriter(new FileWriter(path));
            printWriter = new PrintWriter(bufferedWriter);
        } catch (IOException e) {
            LogUtil.error(e);
        }
        //js执行器加上Log函数
        SCRIPT_ENGINE.put("log",LogUtil.class);
    }

    private static boolean check(Object o, String s) {
        if (bufferedWriter == null) {
            Log.i(s, String.valueOf(o));
            return true;
        }
        return false;
    }

    public static void error(Object object) {
        if (check(object, "error")) return;
        CompletableFuture.runAsync(() -> {
            try {
                if (object instanceof Throwable) {
                    bufferedWriter.append("error : ");//.write(((Throwable) object).getMessage());
                    ((Throwable) object).printStackTrace(printWriter);
                } else {
                    bufferedWriter.append("error : ").write(String.valueOf(object));
                }
                printWriter.flush();
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                Log.i("error", Objects.requireNonNull(e.getMessage()));
            } finally {
                Log.i("error", String.valueOf(object));
            }
        }).join();

    }

    public static void info(Object object) {
        if (check(object, "info")) return;
        try {
            bufferedWriter.append("info : ").write(String.valueOf(object));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            Log.i("info", String.valueOf(object));
        }
    }

    public static void warning(Object object) {
        if (check(object, "warning")) return;
        try {
            bufferedWriter.append("warning : ").write(String.valueOf(object));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            Log.i("warning", String.valueOf(object));
        }
    }

}
