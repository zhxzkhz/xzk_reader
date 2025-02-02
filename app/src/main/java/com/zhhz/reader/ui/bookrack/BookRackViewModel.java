package com.zhhz.reader.ui.bookrack;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.livedata.SingleLiveEvent;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.FileUtil;
import com.zhhz.reader.util.LocalBookUtil;
import com.zhhz.reader.util.OrderlyMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class BookRackViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<BookBean>> data;

    private final MutableLiveData<BookBean> catalogue;

    private final SingleLiveEvent<Operation> operations;

    private final SingleLiveEvent<Boolean> callback;

    public BookRackViewModel() {
        data = new MutableLiveData<>();
        catalogue = new MutableLiveData<>();
        operations = new SingleLiveEvent<>();
        callback = new SingleLiveEvent<>();
        data.setValue(SQLiteUtil.readBooks());
    }

    public void updateBookRack() {
        data.postValue(SQLiteUtil.readBooks());
    }

    public void updateBook(BookBean bookBean) {
        SQLiteUtil.saveBook(bookBean);
    }

    /**
     * 设置书本支持的操作
     * @param operation 操作ID
     */
    public void operationBooks(Operation operation) {
        operations.setValue(operation);
    }

    /**
     * 从书架上移除书本
     * @param ids 书本ID数组
     */
    public void removeBooks(String[] ids) {
        SQLiteUtil.removeBooks(ids);
    }

    /**
     * 删除书本缓存
     * @param ids 书本ID数组
     */
    public void deleteBookCaches(String[] ids) {
        for (String id : ids) {
            FileUtil.deleteFolders(DiskCache.path + File.separator + "book" + File.separator + id);
        }
    }

    /**
     * 删除书本章节缓存
     * @param ids 书本ID数组
     */
    public void deleteBookChapterCaches(String[] ids) {
        for (String id : ids) {
            FileUtil.deleteFolders(DiskCache.path + File.separator + "book" + File.separator + id + File.separator + "book_chapter");
        }
    }

    /**
     * 导入本地书本
     * @param uri 书本位置
     */
    public void importLocalBook(Uri uri) {
        LocalBookUtil.analysisBook(uri, bean -> {
            if (bean != null) {
                SQLiteUtil.saveBook(bean);
                callback.postValue(true);
                //导入成功后更新本地书架
                updateBookRack();
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
                rule = new RuleAnalysis(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBookId() + File.separator + "rule",false);
                rule.getAnalysis().setDetail(bookBean);
            } catch (Exception e) {
                //规则为空代表是本地导入书本
                //e.printStackTrace();
                continue;
            }

            rule.bookDirectory(bookBean.getCatalogue(), (data, url) -> {
                if (data == null) {
                    catalogue.postValue(null);
                } else {
                    if (data.isEmpty()) {
                        catalogue.postValue(null);
                        return;
                    }

                    //读取书本目录章节
                    File file = new File(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBookId() + File.separator + "chapter");
                    OrderlyMap old_map;
                    try (BufferedReader bufferedWriter = new BufferedReader(new FileReader(file))) {
                        old_map = JSONObject.parseObject(bufferedWriter.readLine(), OrderlyMap.class);
                    } catch (IOException ignored) {
                        catalogue.postValue(null);
                        return;
                    }

                    //获取章节最后一章的名字来和以前存储的章节比较，如果最后一章不存在，就代表更新了
                    Iterator<String> iterator = data.keySet().iterator();
                    String key = null;
                    while (iterator.hasNext()) {
                        key = iterator.next();
                    }

                    if (key != null && !old_map.containsKey(key)) {
                        old_map.putAll(data);
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

    public SingleLiveEvent<Operation> getOperations() {
        return operations;
    }

    public SingleLiveEvent<Boolean> getCallback() {
        return callback;
    }

    public enum Operation {
        CACHE,PACK,DELETE
    }

}