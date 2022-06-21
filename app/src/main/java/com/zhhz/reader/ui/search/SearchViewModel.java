package com.zhhz.reader.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.BookBean;

import java.util.ArrayList;

public class SearchViewModel extends ViewModel {

    private MutableLiveData<ArrayList<BookBean>> data;

    public void searchBook(String key){

    }

    public LiveData<ArrayList<BookBean>> getData() {
        return data;
    }
}