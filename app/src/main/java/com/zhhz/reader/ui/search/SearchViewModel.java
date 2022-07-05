package com.zhhz.reader.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.rule.Analysis;
import com.zhhz.reader.rule.RuleAnalysis;

import java.util.ArrayList;
import java.util.Map;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<SearchResultBean>> data;

    public SearchViewModel() {
        this.data = new MutableLiveData<>();
    }

    public void searchBook(String key) {
        for (Map.Entry<String, Analysis> entry : RuleAnalysis.analyses_map.entrySet()) {
            Analysis analysis = entry.getValue();
            analysis.BookSearch(key, (data, msg, label) -> SearchViewModel.this.data.postValue((ArrayList<SearchResultBean>) data), entry.getKey());
        }
    }

    public LiveData<ArrayList<SearchResultBean>> getData() {
        return data;
    }
}