package com.zhhz.reader.util;

import androidx.annotation.NonNull;

import com.zhhz.reader.sql.SQLiteUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DiskCache {

    //用于执行JS
    public static ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");;

    //缓存删除时间标记
    private static boolean cache_delete_tag = true;

    private static long cache_time = System.currentTimeMillis();

    public static String path = null;

    private static void delete_cache(){

        long time = System.currentTimeMillis();

        //大于5分钟执行删除
        if ((time - cache_time) > 1000 * 60 * 5) {
            cache_time = time;
            cache_delete_tag = true;
        }

        if (cache_delete_tag) {
            cache_delete_tag = false;
            String pt = path == null ? SQLiteUtil.context.getExternalCacheDir().getAbsolutePath() : path;
            String file = pt +  File.separator + "Disk_Cache" + File.separator;
            File[] files = new File(file).listFiles();
            //删除缓存时间大于五分钟的
            if (files != null) {
                for (File file1 : files) {
                    if ((time - file1.lastModified()) > 1000 * 60 * 5) {
                        file1.delete();
                    }
                }
            }
        }

    }

    public static Interceptor interceptor = chain -> {
        delete_cache();
        byte[] b;
        //post提交取消缓存
        if (chain.request().method().equalsIgnoreCase("POST")) return chain.proceed(chain.request());
        File file = DiskCache.urlToFile(chain.request().url(), path);
        if (file != null && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)){
                int size = fis.available();
                b = new byte[size];
                if (fis.read(b)!=size) return chain.proceed(chain.request());;
            } catch (IOException e){
               e.printStackTrace();
                return chain.proceed(chain.request());
            }

        } else {
            return chain.proceed(chain.request());
        }

        return new Response.Builder()
                .code(200) //其实code可以随便给
                .protocol(Protocol.HTTP_1_1)
                .protocol(Protocol.HTTP_2)
                .message("本地缓存")
                .body(ResponseBody.create(MediaType.parse("text/html;charset=utf-8"),b))
                .request(chain.request())
                .build();
    };

    private static String encrypt(String dataStr) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes(StandardCharsets.UTF_8));
            byte[] s = m.digest();
            StringBuilder result = new StringBuilder();
            for (byte b : s) {
                result.append(Integer.toHexString((0x000000FF & b) | 0xFFFFFF00).substring(6));
            }
            return result.toString().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataStr;
    }

    public static boolean FileSave(String pt, @NonNull okhttp3.Call call, String data) {
        return FileSave(pt, call.request().url(), data);
    }

    public static boolean FileSave(String pt, HttpUrl call, String data) {
        File file = urlToFile(call, pt);
        if (file == null || file.isFile()) return false;
        try {
            if (!Objects.requireNonNull(file.getParentFile()).isDirectory()) {
                if (!file.getParentFile().mkdirs()) {
                    return false;
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();

        } catch (Exception e) {
            //Log.i("FileSave错误 ->", e.getMessage());
            return false;
        }
        return true;
    }


    private static File urlToFile(HttpUrl call, String pt) {
        String paths = call.encodedPath().substring(1).replace("/", "_");
        pt = pt == null ? SQLiteUtil.context.getExternalCacheDir().getAbsolutePath() : pt;
        String url = pt +  File.separator + "Disk_Cache" + File.separator;
        if (paths.equals("")){
            if (call.encodedQuery() ==null) {
                return null;
            }else {
                return new File(url + encrypt(call.host() + call.encodedQuery()));
            }
        } else {
            if (call.encodedQuery() ==null) {
                return new File(url + encrypt(call.host() + paths));
            }else {
                return new File(url + encrypt(call.host() + paths + call.encodedQuery()));
            }
        }
    }
}
