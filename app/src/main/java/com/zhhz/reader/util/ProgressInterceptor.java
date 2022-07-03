package com.zhhz.reader.util;


import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ProgressInterceptor implements Interceptor {
    public static final Map<String, ProgressListener> LISTENER_MAP = new HashMap<>();

    public static void addListener(String url, ProgressListener listener) {
        LISTENER_MAP.put(url, listener);
    }

    public static void removeListener(String url) {
        LISTENER_MAP.remove(url);
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        String url = request.url().toString();
        ResponseBody body = response.body();
        long l = -1L;
        String length = response.header("Content-Length") == null ? response.header("content-length") : response.header("Content-Length");
        if (length != null)
            l = Long.parseLong(length);
        return response.newBuilder().body(new ProgressResponseBody(url, body, l)).build();
    }

}

