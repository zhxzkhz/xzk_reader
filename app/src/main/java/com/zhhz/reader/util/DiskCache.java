package com.zhhz.reader.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sun.script.javascript.RhinoScriptEngine;
import com.zhhz.reader.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.script.ScriptEngine;

import cn.hutool.crypto.digest.MD5;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DiskCache {

    //测试缓存时间调整到 300 分钟
    private static final long cache_time = 1000 * 60 * 5;
    //用于执行JS
    public static final ScriptEngine SCRIPT_ENGINE = new RhinoScriptEngine();
    public static String path = "/storage/emulated/0/星☆空";
    //缓存删除时间标记
    public static boolean cache_delete_tag = true;
    private static long currentTimeMillis = System.currentTimeMillis();
    public static final Interceptor interceptor = chain -> {
        delete_cache();
        byte[] b;
        //post提交取消缓存
        if (chain.request().method().equalsIgnoreCase("POST")) {
            Request request = chain.request();
            HttpUrl beforeUrl = request.url();
            Response response = chain.proceed(request);
            HttpUrl afterUrl = response.request().url();
            //根据url判断是否是重定向
            if(!beforeUrl.equals(afterUrl)) {
                //重新请求
                Request newRequest = request.newBuilder().url(response.request().url()).build();
                response.close();
                return chain.proceed(newRequest);
            } else {
                return response;
            }
        }
        File file = DiskCache.urlToFile(chain.request().url(), path);
        if (file != null && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                int size = fis.available();
                b = new byte[size];
                if (fis.read(b) != size) return chain.proceed(chain.request());
            } catch (IOException e) {
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
                .body(ResponseBody.create(b,MediaType.parse("text/html;charset=utf-8")))
                .request(chain.request())
                .build();
    };

    static {
        CompletableFuture.runAsync(() -> {
            File f = new File(path);
            if (!f.isDirectory() || (f.isDirectory() && !f.canWrite())) {
                if (!new File(path).mkdirs()) {
                    path = Objects.requireNonNull(MyApplication.context.getExternalFilesDir("")).getAbsolutePath();
                }
            }
        });

    }

    /**
     * 清除五分钟前的缓存
     */
    public static void delete_cache() {
        delete_cache(false);
    }

    /**
     * 清除缓存
     * @param bool 是否强制清除所有
     */
    public static void delete_cache(boolean bool) {

        long time = System.currentTimeMillis();

        //大于5分钟执行删除
        if ((time - currentTimeMillis) > cache_time) {
            currentTimeMillis = time;
            cache_delete_tag = true;
        }

        if (cache_delete_tag || bool) {
            cache_delete_tag = false;
            String pt = path == null ? Objects.requireNonNull(MyApplication.context.getExternalCacheDir()).getAbsolutePath() : path;
            String file = pt + File.separator + "Disk_Cache" + File.separator;
            File[] files = new File(file).listFiles();
            //删除缓存时间大于五分钟的
            if (files != null) {
                for (File file1 : files) {
                    if (((time - file1.lastModified()) > cache_time)| bool) {
                        if (file1.delete()) {
                            LogUtil.info("delete_cache -> 缓存已清除");
                        }
                    }
                }
            }
        }

    }

    public static void FileSave(String pt, @NonNull okhttp3.Call call, byte[] data) {
        FileSave(pt, call.request().url(), data);
    }

    public static void FileSave(String pt, HttpUrl call, byte[] data) {
        File file = urlToFile(call, pt);
        if (file == null || file.isFile()) return ;
        try {
            if (!Objects.requireNonNull(file.getParentFile()).isDirectory()) {
                if (!file.getParentFile().mkdirs()) {
                    return ;
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }


    @Nullable
    public static File urlToFile(HttpUrl call, String pt) {
        String paths = call.encodedPath().substring(1).replace("/", "_");
        pt = pt == null ? Objects.requireNonNull(MyApplication.context.getExternalCacheDir()).getAbsolutePath() : pt;
        String url = pt + File.separator + "Disk_Cache" + File.separator;
        if (paths.isEmpty()) {
            if (call.encodedQuery() == null) {
                return null;
            } else {
                return new File(url + MD5.create().digestHex(call.host() + call.encodedQuery()));
            }
        } else {
            if (call.encodedQuery() == null) {
                return new File(url + MD5.create().digestHex(call.host() + paths));
            } else {
                return new File(url + MD5.create().digestHex(call.host() + paths + call.encodedQuery()));
            }
        }
    }
}
