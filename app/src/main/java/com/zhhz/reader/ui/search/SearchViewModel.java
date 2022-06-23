package com.zhhz.reader.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.rule.RuleAnalysis;

import java.util.ArrayList;
import java.util.Iterator;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<SearchResultBean>> data;

    public SearchViewModel() {
        this.data = new MutableLiveData<>();
    }

    public void searchBook(String key){
        RuleAnalysis.analyses_map.forEach((s, analysis) -> {
            int index=0;
            Iterator<String> iterator = RuleAnalysis.analyses_map.keySet().iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                if (s.equals(iterator.next())) {
                    index = i;
                    break;
                }
            }
            analysis.BookSearch(key, (data, msg, label) -> SearchViewModel.this.data.setValue(((ArrayList<SearchResultBean>) data)),index);
        });
    }

    public LiveData<ArrayList<SearchResultBean>> getData() {
        return data;
    }
}