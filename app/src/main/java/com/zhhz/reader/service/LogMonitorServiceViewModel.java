package com.zhhz.reader.service;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class LogMonitorServiceViewModel extends ViewModel {
    private MutableLiveData<String> data;

    public LogMonitorServiceViewModel() {
        this.data = new MutableLiveData<>();
    }

    public void addData(String s){
        data.setValue(s);
    }

    public MutableLiveData<String> getData() {
        return data;
    }
}
