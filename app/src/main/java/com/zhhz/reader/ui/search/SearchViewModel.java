package com.zhhz.reader.ui.search;

import android.content.Intent;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.activity.DetailedActivity;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.rule.Analysis;
import com.zhhz.reader.rule.RuleAnalysis;

import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<SearchResultBean>> data;

    public SearchViewModel() {
        this.data = new MutableLiveData<>();
    }

    public void searchBook(String key) {
        //用于判断是否进行搜索
        boolean bool = true;
        for (Map.Entry<String, Analysis> entry : RuleAnalysis.analyses_map.entrySet()) {
            Analysis analysis = entry.getValue();
            if (analysis.isHaveSearch()) {
                bool = false;
                analysis.BookSearch(key, (data, msg, label) -> SearchViewModel.this.data.postValue((ArrayList<SearchResultBean>) data), entry.getKey());
            }
        }
        if (bool) {
            SearchViewModel.this.data.postValue(new ArrayList<>());
        }
    }

    /**
     * 用于直接访问书本详细地址
     * 因为有些书无法通过站内搜索，所以直接使用地址访问
     * 如果是地址直接转跳详细截面
     * @param key 关键词
     * @return SearchResultBean
     */
    public SearchResultBean isUrl(String key){
        for (Map.Entry<String, Analysis> entry : RuleAnalysis.analyses_map.entrySet()) {
            if (key.matches("https?://" + entry.getValue().getUrl() + "/(.+)")){
                SearchResultBean bean = new SearchResultBean();
                bean.setUrl(key);
                List<String> source = new ArrayList<>();
                source.add(entry.getKey());
                bean.setSource(source);
                return bean;
            }
        }
        return null;
    }

    public MutableLiveData<ArrayList<SearchResultBean>> getData() {
        return data;
    }
}