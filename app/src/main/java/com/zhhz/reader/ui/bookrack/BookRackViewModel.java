package com.zhhz.reader.ui.bookrack;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.sql.SQLiteUtil;

import java.util.ArrayList;

public class BookRackViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<BookBean>> data;

    public BookRackViewModel() {
        data = new MutableLiveData<>();
        //data.postValue(SQLiteUtil.readBooks());
        ArrayList<BookBean> list = new ArrayList<>();
        BookBean bookBean = new BookBean();
        bookBean.setBook_id("1");
        bookBean.setTitle("测试数据\n测试数据");
        bookBean.setAuthor("作者");
        bookBean.setCover("https://bookcover.yuewen.com/qdbimg/349573/1033830145/180");
        list.add(bookBean);
        list.add(bookBean);
        list.add(bookBean);
        list.add(bookBean);

        data.setValue(list);
    }

    public LiveData<ArrayList<BookBean>> getData() {
        return data;
    }
}