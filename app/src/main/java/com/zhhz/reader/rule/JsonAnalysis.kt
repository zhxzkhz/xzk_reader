package com.zhhz.reader.rule;

import com.alibaba.fastjson.JSONObject;
import com.zhhz.reader.bean.BookBean;

import java.io.IOException;

public class JsonAnalysis extends Analysis {
    public JsonAnalysis(String path) throws IOException {
        super(path);
    }

    public JsonAnalysis(JSONObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public void BookSearch(String key_word, CallBack callback, String md5) {

    }

    @Override
    public void BookDirectory(String url, CallBack callback) {

    }

    @Override
    public void BookDetail(String url, CallBack callback) {

    }

    @Override
    public void BookChapters(BookBean book, String url, CallBack callback, Object random) {

    }

    @Override
    public void BookContent(String url, CallBack callback, Object random) {

    }
}
