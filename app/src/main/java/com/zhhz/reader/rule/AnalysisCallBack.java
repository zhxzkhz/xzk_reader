package com.zhhz.reader.rule;

import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.HttpResponseBean;
import com.zhhz.reader.bean.SearchResultBean;

import java.util.LinkedHashMap;
import java.util.List;

public class AnalysisCallBack {

    public interface CallBack {
        void run(HttpResponseBean httpResponseBean);
    }

    public interface SearchCallBack {
        void run(List<SearchResultBean> list);
    }

    public interface DetailCallBack {
        void run(BookBean bookBean);
    }

    public interface DirectoryCallBack {
        void run(LinkedHashMap<String, String> map,String url);
    }

    public interface ContentCallBack {
        void run(HttpResponseBean data, Object tag);
    }

    public interface LogError {
        void log(String error);
    }

}