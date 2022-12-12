package com.zhhz.reader.ui.bookrack;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.livedata.SingleLiveEvent;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.LocalBookUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

public class BookRackViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<BookBean>> data;

    private final MutableLiveData<BookBean> catalogue;

    private final SingleLiveEvent<Integer> operation;

    private final SingleLiveEvent<Boolean> callback;

    public BookRackViewModel() {
        data = new MutableLiveData<>();
        catalogue = new MutableLiveData<>();
        operation = new SingleLiveEvent<>();
        callback = new SingleLiveEvent<>();
        data.setValue(SQLiteUtil.readBooks());
    }

    public void updateBooks() {
        data.postValue(SQLiteUtil.readBooks());
    }

    public void updateBook(BookBean bookBean) {
        SQLiteUtil.saveBook(bookBean);
    }

    public void operationBooks(Integer integer) {
        operation.setValue(integer);
    }

    public void removeBooks(String[] s) {
        SQLiteUtil.removeBooks(s);
    }

    /**
     * 导入本地书本
     *
     * @param uri 书本位置
     */
    public void importLocalBook(Uri uri) {
        LocalBookUtil.analysisBook(uri, bean -> {
            if (bean != null) {
                SQLiteUtil.saveBook(bean);
                callback.postValue(true);
                //导入成功后更新本地书架
                updateBooks();
            } else {
                callback.postValue(false);
            }
        });

    }

    /**
     * 更新目录
     */
    public void updateCatalogue() {
        for (BookBean bookBean : Objects.requireNonNull(data.getValue())) {
            RuleAnalysis rule;
            try {
                rule = new RuleAnalysis(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBook_id() + File.separator + "rule",false);
                rule.getAnalysis().setDetail(bookBean);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            rule.bookDirectory(bookBean.getCatalogue(), (data, url) -> {
                if (data == null) {
                    catalogue.postValue(null);
                } else {
                    if (((LinkedHashMap<String, String>) data).size() == 0) {
                        catalogue.postValue(null);
                        return;
                    }

                    //读取书本目录章节
                    File file = new File(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBook_id() + File.separator + "chapter");
                    LinkedHashMap<String, String> old_map;
                    try (BufferedReader bufferedWriter = new BufferedReader(new FileReader(file))) {
                        old_map = JSONObject.parseObject(bufferedWriter.readLine(), new TypeReference<LinkedHashMap<String, String>>() {
                        }.getType());
                    } catch (IOException ignored) {
                        catalogue.postValue(null);
                        return;
                    }

                    //获取章节最后一章的名字来和以前存储的章节比较，如果最后一章不存在，就代表更新了
                    Iterator<String> iterator = ((LinkedHashMap<String, String>) data).keySet().iterator();
                    String key = null;
                    while (iterator.hasNext()) {
                        key = iterator.next();
                    }

                    if (!old_map.containsKey(key)) {
                        old_map.putAll((LinkedHashMap<String, String>) data);
                        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                            bufferedWriter.write(JSON.toJSONString(old_map));
                        } catch (IOException ignored) {
                        }
                        if (url != null) {
                            if (!url.isEmpty()) bookBean.setCatalogue(url);
                        }
                        bookBean.setUpdate(true);
                        SQLiteUtil.saveBook(bookBean);
                        catalogue.postValue(bookBean);
                    } else {
                        catalogue.postValue(null);
                    }
                }
            });
        }
    }

    public LiveData<ArrayList<BookBean>> getData() {
        return data;
    }

    public MutableLiveData<BookBean> getCatalogue() {
        return catalogue;
    }

    public SingleLiveEvent<Integer> getOperation() {
        return operation;
    }

    public SingleLiveEvent<Boolean> getCallback() {
        return callback;
    }
}