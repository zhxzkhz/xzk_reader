package com.zhhz.reader.ui.detailed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSON;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;

public class DetailedViewModel extends ViewModel {

    private final MutableLiveData<BookBean> data;

    private final MutableLiveData<LinkedHashMap<String, String>> data_catalogue;

    public DetailedViewModel() {
        data = new MutableLiveData<>();
        data_catalogue = new MutableLiveData<>();
    }

    public void queryDetailed(SearchResultBean bean, int index) {
        Objects.requireNonNull(RuleAnalysis.analyses_map.get(bean.getSource().get(index))).BookDetail(bean.getUrl(), (data, msg, isComic) -> {
            BookBean book = (BookBean) data;
            if (book.getTitle() == null || book.getTitle().isEmpty()){
                book.setTitle(bean.getTitle());
            }
            if (book.getAuthor() == null || book.getAuthor().isEmpty()){
                book.setAuthor(bean.getAuthor());
            }
            if (book.getCover() == null || book.getCover().isEmpty()){
                book.setCover(bean.getCover());
            }
            book.setBook_id(StringUtil.getMD5(book.getTitle() + "▶☀" + isComic + "☀◀" + book.getAuthor()));
            DetailedViewModel.this.data.postValue(book);
        });
    }

    public void queryCatalogue(String url, SearchResultBean bean, int index) {
        Objects.requireNonNull(RuleAnalysis.analyses_map.get(bean.getSource().get(index))).BookDirectory(url, (data, msg, label) -> DetailedViewModel.this.data_catalogue.postValue((LinkedHashMap<String, String>) data));
    }

    public void saveProgress(String id, int progress) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(DiskCache.path + File.separator + "book" + File.separator + id + File.separator + "progress"));) {
            bufferedWriter.write(progress + ",0");
        } catch (IOException ignored) {
        }
    }

    public int[] readProgress(String id) {
        // 0 章节进度 1 阅读进度
        int[] pro = new int[2];
        File file = new File(DiskCache.path + File.separator + "book" + File.separator + id + File.separator + "progress");
        if (!file.isFile()) {
            return pro;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String[] progress = bufferedReader.readLine().split(",");
            bufferedReader.close();
            pro[0] = Integer.parseInt(progress[0]);
            pro[1] = Integer.parseInt(progress[1]);
            return pro;
        } catch (IOException | NumberFormatException e) {
            return pro;
        }
    }

    public void saveDirectory(String id) {
        File file = new File(DiskCache.path + File.separator + "book" + File.separator + id + File.separator + "chapter");
        if (!Objects.requireNonNull(file.getParentFile()).isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                System.out.println("目录创建失败");
            }
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(JSON.toJSONString(data_catalogue.getValue()));
        } catch (IOException ignored) {
        }
    }

    public void saveRule(SearchResultBean bean, String id, int index) {
        File file = new File(DiskCache.path + File.separator + "book" + File.separator + id + File.separator + "rule");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(Objects.requireNonNull(RuleAnalysis.analyses_map.get(bean.getSource().get(index))).getJson().toJSONString());
        } catch (IOException ignored) {
        }
    }

    public LiveData<BookBean> getData() {
        return data;
    }

    public LiveData<LinkedHashMap<String, String>> getDataCatalogue() {
        return data_catalogue;
    }

}