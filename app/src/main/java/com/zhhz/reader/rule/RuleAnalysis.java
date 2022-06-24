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
    public static LinkedHashMap<String,Analysis> analyses_map = new LinkedHashMap<>();

    private Analysis analysis;

    public RuleAnalysis(String path) throws IOException {
        this(path,false);
    }

    public RuleAnalysis(String path,Boolean bool) throws IOException {
        JSONObject jsonObject = Analysis.readText(path);
        int type = jsonObject.getIntValue("type");
        // 0 为 jsoup ， 1 为 json
        if ( type== 0){
            analysis = new JsoupAnalysis(jsonObject);
        } else if (type == 1) {

        }
        if (bool) analyses_map.put(StringUtil.getMD5(jsonObject.toJSONString()),analysis);
    }

    public void BookSearch(String key_word, Analysis.CallBack callback, String md5){
        analysis.BookSearch(key_word, callback, md5);
    }

    public void BookDirectory(String url, Analysis.CallBack callback){
        analysis.BookDirectory(url, callback);
    }

    public void BookDetail(String url, Analysis.CallBack callback){
        analysis.BookDetail(url, callback);
    }

    public void BookChapters(BookBean url, Analysis.CallBack callback, Object random){
        analysis.BookChapters(url, callback, random);
    }

}
