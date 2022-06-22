package com.zhhz.reader.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.SearchBean;
import com.zhhz.reader.rule.RuleAnalysis;

import java.util.Iterator;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<SearchBean>> data;

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
            analysis.BookSearch(key, (data, msg, label) -> SearchViewModel.this.data.setValue(((List<SearchBean>) data)),index);
        });
    }

    public LiveData<List<SearchBean>> getData() {
        return data;
    }
}