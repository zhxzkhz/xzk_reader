package com.zhhz.reader.ui.bookrack;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.util.DiskCache;

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

    private final MutableLiveData<Integer> operation;

    public BookRackViewModel() {
        data = new MutableLiveData<>();
        catalogue = new MutableLiveData<>();
        operation = new MutableLiveData<>();
        data.setValue(SQLiteUtil.readBooks());
    }

    public void updateBooks(){
        data.postValue(SQLiteUtil.readBooks());
    }

    public void updateBook(BookBean bookBean){
        SQLiteUtil.saveBook(bookBean);
    }

    public void operationBooks(Integer integer){
        operation.setValue(integer);
    }

    public void removeBooks(String[] s){
        SQLiteUtil.removeBooks(s);
    }

    /**
     * 更新目录
     */
    public void updateCatalogue() {
        for (BookBean bookBean : Objects.requireNonNull(data.getValue())) {
            RuleAnalysis rule;
            try {
                rule = new RuleAnalysis(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBook_id() + File.separator + "rule");
                rule.getAnalysis().setDetail(bookBean);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            rule.BookDirectory(bookBean.getCatalogue(), (data, msg, url) -> {
                if (data == null) {
                    catalogue.postValue(null);
                } else {
                    LinkedHashMap<String, String> mv = (LinkedHashMap<String, String>) data;
                    if (mv.size() == 0) {
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
                    Iterator<String> iterator = mv.keySet().iterator();
                    String key = null;
                    while (iterator.hasNext()) {
                        key = iterator.next();
                    }

                    if (!old_map.containsKey(key)) {
                        old_map.putAll(mv);
                        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                            bufferedWriter.write(JSON.toJSONString(old_map));
                        } catch (IOException ignored) {
                        }
                        bookBean.setCatalogue(String.valueOf(url));
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

    public MutableLiveData<Integer> getOperation() {
        return operation;
    }
}