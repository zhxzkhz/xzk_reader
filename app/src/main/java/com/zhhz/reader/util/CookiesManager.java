package com.zhhz.reader.util;

import androidx.annotation.NonNull;

import com.zhhz.reader.MyApplication;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * 自动管理Cookies
 */
public class CookiesManager implements CookieJar {
    private final PersistentCookieStore cookieStore = new PersistentCookieStore(MyApplication.context);

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        if (!cookies.isEmpty()) {
            for (Cookie item : cookies) {
                cookieStore.add(url, item);
            }
        }
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        return cookieStore.get(url);
    }
}
