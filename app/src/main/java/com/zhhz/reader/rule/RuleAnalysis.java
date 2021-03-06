package com.zhhz.reader.rule;

import com.alibaba.fastjson.JSONObject;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.StringUtil;

import java.io.IOException;
import java.util.LinkedHashMap;

import okhttp3.OkHttpClient;

public class RuleAnalysis {

    public static OkHttpClient client = new OkHttpClient.Builder().addInterceptor(DiskCache.interceptor).build();

    //储存已加载的规则
    public static LinkedHashMap<String, Analysis> analyses_map = new LinkedHashMap<>();

    private Analysis analysis;

    public RuleAnalysis(String path) throws IOException {
        this(path, false);
    }

    public RuleAnalysis(String path, Boolean bool) throws IOException {
        this(Analysis.readText(path), bool);
    }

    public RuleAnalysis(JSONObject jsonObject, Boolean bool) {
        int type = jsonObject.getIntValue("type");
        // 0 为 jsoup ， 1 为 json
        if (type == 0) {
            analysis = new JsoupAnalysis(jsonObject);
        } else if (type == 1) {
            analysis = new JsonAnalysis(jsonObject);
        }
        if (bool) analyses_map.put(StringUtil.getMD5(analysis.getName()), analysis);
    }

    public void BookSearch(String key_word, Analysis.CallBack callback, String md5) {
        analysis.BookSearch(key_word, callback, md5);
    }

    /**
     * 获取书本目录
     *
     * @param url      目录地址
     * @param callback 回调函数
     */
    public void BookDirectory(String url, Analysis.CallBack callback) {
        analysis.BookDirectory(url, callback);
    }

    public void BookDetail(String url, Analysis.CallBack callback) {
        analysis.BookDetail(url, callback);
    }

    public void BookChapters(BookBean bookBean, String url, Analysis.CallBack callback, Object random) {
        analysis.BookChapters(bookBean, url, callback, random);
    }

    public Analysis getAnalysis() {
        return analysis;
    }
}
