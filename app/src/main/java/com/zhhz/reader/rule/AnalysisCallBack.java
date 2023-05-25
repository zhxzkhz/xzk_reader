package com.zhhz.reader.rule;

import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.HttpResponseBean;
import com.zhhz.reader.bean.SearchResultBean;

import java.util.LinkedHashMap;
import java.util.List;

public class AnalysisCallBack {

    public interface CallBack {
        void accept(HttpResponseBean httpResponseBean);
    }

    public interface SearchCallBack {
        void accept(List<SearchResultBean> list);
    }

    public interface DetailCallBack {
        void accept(BookBean bookBean);
    }

    public interface DirectoryCallBack {
        void accept(LinkedHashMap<String, String> map, String url);
    }

    public interface ContentCallBack {
        void accept(HttpResponseBean data, Object tag);
    }

    public interface LogError {
        void log(String error);
    }

}