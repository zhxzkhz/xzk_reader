package com.zhhz.reader.ui.detailed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.rule.Analysis;
import com.zhhz.reader.rule.RuleAnalysis;

import java.util.LinkedHashMap;
import java.util.Objects;

public class DetailedViewModel extends ViewModel {

    private final MutableLiveData<BookBean> data;

    private final MutableLiveData<LinkedHashMap<String,String>> data_catalogue;

    public DetailedViewModel() {
        data = new MutableLiveData<>();
        data_catalogue = new MutableLiveData<>();
    }

    public void queryDetailed(SearchResultBean bean,int index){
        Objects.requireNonNull(RuleAnalysis.analyses_map.get(bean.getSource().get(index))).BookDetail(bean.getUrl(), (data, msg, label) -> DetailedViewModel.this.data.postValue((BookBean) data));
    }

    public void queryCatalogue(String url,SearchResultBean bean,int index){
        Objects.requireNonNull(RuleAnalysis.analyses_map.get(bean.getSource().get(index))).BookDirectory(url, (data, msg, label) -> DetailedViewModel.this.data_catalogue.postValue((LinkedHashMap<String,String>) data));
    }

    public LiveData<BookBean> getData() {
        return data;
    }

    public LiveData<LinkedHashMap<String,String>> getDataCatalogue() {
        return data_catalogue;
    }

}