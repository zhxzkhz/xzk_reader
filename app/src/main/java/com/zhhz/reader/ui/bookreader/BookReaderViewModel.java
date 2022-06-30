package com.zhhz.reader.ui.bookreader;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.util.DiskCache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public class BookReaderViewModel extends ViewModel {

    private final MutableLiveData<LinkedHashMap<String, String>> data_catalogue;

    private final MutableLiveData<HashMap<String, String>> data_content;

    private final MutableLiveData<String> chapters;

    private final ArrayList<String> catalogue;

    private RuleAnalysis rule;
    //目录进度
    private int progress = 0;
    //章节进度
    private int start = 0;
    //唯一值
    private String uuid;

    private BookBean book;
    //缓存错误次数
    private int cache_error = 0;

    public BookReaderViewModel() {
        this.data_catalogue = new MutableLiveData<>();
        this.data_content = new MutableLiveData<>();
        this.chapters = new MutableLiveData<>();
        this.catalogue = new ArrayList<>();
    }

    public void setBook(BookBean book) {
        try {
            rule = new RuleAnalysis(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "rule");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.book = book;
    }

    public BookBean getBook() {
        return book;
    }

    public void queryCatalogue() {
        File file = new File(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "chapter");
        try (BufferedReader bufferedWriter = new BufferedReader(new FileReader(file))) {
            LinkedHashMap<String, String> map = JSONObject.parseObject(bufferedWriter.readLine(), new TypeReference<LinkedHashMap<String, String>>() {
            }.getType());
            catalogue.addAll(map.keySet());
            data_catalogue.setValue(map);
        } catch (IOException ignored) {
        }
    }

    public void getContent() {
        getContent(false);
    }

    /**
     *  获取内容
     * @param bool 是否往上一页翻
     */
    public void getContent(boolean bool) {
        uuid = UUID.randomUUID().toString();
        chapters.setValue(catalogue.get(progress));
        String url = Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress));
        rule.BookChapters(book, url, (data, msg, label) -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("end",String.valueOf(bool));
            if (uuid.equals(label)) {
                if (msg != null) {
                    map.put("error", msg.toString());
                } else {
                    map.put("content", data.toString());
                    //自动缓存下一章
                    if (isHaveNextChapters()){
                        cacheBook(progress);
                    }
                }
                data_content.postValue(map);
            }
        }, uuid);
    }

    public void saveProgress(int progress) {
        saveProgress(progress, 0);
    }

    public void saveProgress(int progress, int start) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "progress"))) {
            bufferedWriter.write(progress + "," + start);
        } catch (IOException ignored) {
        }
    }

    /**
     *  获取阅读章节和位置
     * @return 章节和位置
     */
    public int[] readProgress() {
        // 0 章节进度 1 阅读进度
        int[] pro = new int[2];
        File file = new File(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "progress");
        if (!file.isFile()) {
            saveProgress(0);
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void cacheBook(int progress){
        cacheBook(progress,false);
    }

    /**
     * 缓存下一章节
     * @param progress 开始缓存位置
     * @param bool 是否缓存所有章节
     */
    public void cacheBook(int progress,boolean bool){
        progress = progress>-1 ? this.progress : progress;
        progress++;
        uuid = UUID.randomUUID().toString();
        String url = Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress));
        int finalProgress = progress;
        rule.BookChapters(book, url, (data, msg, label) -> {
                if (msg != null) {
                    cache_error++;
                    //失败三次取消缓存
                    if (cache_error < 3){
                        cacheBook(finalProgress,bool);
                    } else {
                        cache_error = 0;
                    }
                } else if (bool){
                    if (isHaveNextChapters(finalProgress)){
                        cacheBook(finalProgress+1, true);
                    }
                }
        }, uuid);
    }

    public boolean isHaveNextChapters() {
        return isHaveNextChapters(-1);
    }

    public boolean isHaveNextChapters(int progress) {
        return progress != _haveNextChapters(progress);
    }

    private int _haveNextChapters(int progress) {
        int progress_temp = progress > -1 ? progress : this.progress;
        if (progress_temp + 1 >= catalogue.size()) {
            return progress;
        }
        while (isSubtitle(++progress_temp)) {
            if (progress_temp + 1 >= catalogue.size()) {
                return progress;
            }
        }
        return progress_temp;
    }

    /**
     * @return 是否有上一章节
     */
    public boolean isHavePreviousChapters() {
        return isHavePreviousChapters(-1);
    }

    /**
     * @param progress 章节位置
     * @return 是否有上一章节
     */
    public boolean isHavePreviousChapters(int progress) {
        return progress != _havePreviousChapters(progress);
    }

    private int _havePreviousChapters(int progress) {
        int progress_temp = progress > -1 ? progress : this.progress;
        if (progress_temp - 1 < 0) {
            return progress;
        }
        while (isSubtitle(--progress_temp)) {
            if (progress_temp - 1 < 0) {
                return progress;
            }
        }
        return progress_temp;
    }

    public void loadNextChapters() {
        progress = _haveNextChapters(progress);
        getContent();
    }

    public void loadPreviousChapters(){
        progress = _havePreviousChapters(progress);
        getContent(true);
    }

    public void jumpChapters(int pos){
        start = 0;
        progress = pos;
        saveProgress(progress);
        getContent();
    }

    public boolean isSubtitle(int progress) {
        return Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress)) == null;
    }

    public boolean isComic(){
        return rule.getAnalysis().isComic();
    }

    public MutableLiveData<LinkedHashMap<String, String>> getDataCatalogue() {
        return data_catalogue;
    }

    public MutableLiveData<HashMap<String, String>> getDataContent() {
        return data_content;
    }

    public MutableLiveData<String> getChapters() {
        return chapters;
    }
}