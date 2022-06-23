package com.zhhz.reader.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.rule.Analysis;
import com.zhhz.reader.rule.RuleAnalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<SearchResultBean>> data;

    public SearchViewModel() {
        this.data = new MutableLiveData<>();
    }

    public void searchBook(String key){
        for (Map.Entry<String, Analysis> entry : RuleAnalysis.analyses_map.entrySet()) {
            String s = entry.getKey();
            Analysis analysis = entry.getValue();
            int index = 0;
            Iterator<String> iterator = RuleAnalysis.analyses_map.keySet().iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                if (s.equals(iterator.next())) {
                    index = i;
                    break;
                }
            }
            long time = System.currentTimeMillis();
            analysis.BookSearch(key, (data, msg, label) -> {
                System.out.printf("耗时 -> %d%n", System.currentTimeMillis() -time);
                SearchViewModel.this.data.postValue(((ArrayList<SearchResultBean>) data));
            }, index);
        }
    }

    public LiveData<ArrayList<SearchResultBean>> getData() {
        return data;
    }
}