package com.zhhz.reader.rule;

import com.zhhz.reader.util.DiskCache;

import okhttp3.OkHttpClient;

public class RuleAnalysis {
    public static OkHttpClient client = new OkHttpClient.Builder().addInterceptor(DiskCache.interceptor).build();
}
