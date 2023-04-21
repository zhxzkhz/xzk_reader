package com.zhhz.reader.ui.bookreader;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.FileUtil;
import com.zhhz.reader.util.LogUtil;
import com.zhhz.reader.util.NotificationUtil;
import com.zhhz.reader.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import cn.hutool.core.util.ObjectUtil;

public class BookReaderViewModel extends ViewModel {

    public final ArrayList<String> catalogue;
    private final MutableLiveData<LinkedHashMap<String, String>> data_catalogue;
    private final MutableLiveData<HashMap<String, Object>> data_content;
    private final MutableLiveData<String> chapters;
    private final MutableLiveData<String> font_setting;
    private final ArrayList<GlideUrl> comic_list;
    //章节页数表，用于无限滑动记录章节页数
    public final ArrayList<Integer> comic_chapters;
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
    private LazyHeaders headers;

    //加载状态
    private boolean loading = false;

    private boolean localBooks = false;

    public BookReaderViewModel() {
        this.data_catalogue = new MutableLiveData<>();
        this.data_content = new MutableLiveData<>();
        this.chapters = new MutableLiveData<>();
        this.font_setting = new MutableLiveData<>();
        this.catalogue = new ArrayList<>();
        this.comic_list = new ArrayList<>();
        this.comic_chapters = new ArrayList<>();
    }

    public ArrayList<String> getCatalogue() {
        return catalogue;
    }

    public boolean isLocalBooks() {
        return localBooks;
    }

    public BookBean getBook() {
        return book;
    }

    public void setBook(BookBean book) {
        this.book = book;
        //rule 为空代表是本地书本
        if (!new File(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "rule").isFile()) {
            localBooks = true;
            return;
        }
        try {
            rule = new RuleAnalysis(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "rule",false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LazyHeaders.Builder header = new LazyHeaders.Builder();
        if (rule.getAnalysis().getJson().getImgHeader() != null) {
            if (ObjectUtil.isNotEmpty(rule.getAnalysis().getJson().getImgHeader().getHeader())) {
                JSONObject header_x = JSONObject.parseObject(rule.getAnalysis().getJson().getImgHeader().getHeader());
                for (Map.Entry<String, Object> entry : header_x.entrySet()) {
                    header.addHeader(entry.getKey(), (String) entry.getValue());
                }
                header_x.clear();
            }
            if (rule.getAnalysis().getJson().getImgHeader().getReuse()) {
                JSONObject header_x = JSONObject.parseObject(rule.getAnalysis().getJson().getHeader());
                for (Map.Entry<String, Object> entry : header_x.entrySet()) {
                    header.addHeader(entry.getKey(), (String) entry.getValue());
                }
                header_x.clear();
            }
        }
        headers = header.build();
    }

    /**
     * 获取章节
     */
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

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public void getContent() {
        getContent(false);
    }

    /**
     * 获取内容
     *
     * @param bool 是否往上一页翻
     */
    public void getContent(boolean bool) {
        uuid = UUID.randomUUID().toString();
        chapters.setValue(catalogue.get(progress));
        String url = Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress));

        HashMap<String, Object> map = new HashMap<>();
        if (url == null) {
            map.put("error", "章节地址为空");
            data_content.postValue(map);
            return;
        }
        map.put("end", bool);
        // url 第一个字符是 / 代表是本地章节
        if (url.startsWith("/")) {
            byte[] bytes = FileUtil.readFile(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "book_chapter" + url);
            if (bytes == null) {
                map.put("error", "内容获取失败");
            } else {
                map.put("content", new String(bytes));
            }
            data_content.postValue(map);
        } else {
            loading = true;
            rule.bookChapters(book, url, (data, label) -> {
                loading = false;
                if (uuid.equals(label)) {
                    if (data.isStatus()){
                        map.put("content", data.getData());
                        //自动缓存下一章
                        if (isHaveNextChapters()) {
                            cacheBook(progress);
                        }
                    } else {
                        map.put("error", data.getError());
                    }

                    data_content.postValue(map);
                }
            }, uuid);
        }
    }

    /**
     * 获取内容
     *
     * @param bool 是否加载历史记录位置
     */
    public void getContentComic(boolean bool) {
        comic_list.clear();
        uuid = UUID.randomUUID().toString();
        chapters.setValue(catalogue.get(progress));
        String url = Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress));

        HashMap<String, Object> map = new HashMap<>();
        if (url == null) {
            map.put("error", "章节地址为空");
            data_content.postValue(map);
            return;
        }
        loading = true;
        rule.bookChapters(book, url, (data, label) -> {
            loading = false;
            map.put("end", bool);
            if (uuid.equals(label)) {
                if (data.isStatus()){
                    String[] arr = data.getData().split("\n");
                    for (String s : arr) {
                        comic_list.add(new GlideUrl(s, headers));
                    }
                    comic_chapters.add(comic_list.size());
                    map.put("content", comic_list);
                } else {
                    map.put("error", data.getError());
                }
                data_content.postValue(map);
            }
        }, uuid);
    }

    /**
     * 清除当前章节缓存，重新加载
     */
    public void clearCurrentCache(){
        DiskCache.delete_cache(true);
        String url = Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress));
        if (url != null) {
            try {
                //Files.delete(Paths.get(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "book_chapter" + File.separator + url.substring(url.lastIndexOf('/') + 1)));
                Files.delete(Paths.get(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "book_chapter" + File.separator + StringUtil.getMD5(url)));
            } catch (IOException e) {
                LogUtil.error(e);
            }
        } else {
            LogUtil.info("缓存清除失败 -> " + catalogue.get(progress));
        }
        getContent();
    }

    public void saveProgress(int progress) {
        saveProgress(progress, 0);
    }

    public void saveProgressComic() {
        int[] a = current_progress_page(start);
        saveProgress(a[0], a[1]);
    }

    public void saveProgress(int progress, int start) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(DiskCache.path + File.separator + "book" + File.separator + book.getBook_id() + File.separator + "progress"))) {
            bufferedWriter.write(progress + "," + start);
        } catch (IOException ignored) {
        }
    }


    /**
     * 获取阅读章节和位置
     *
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

    public ArrayList<GlideUrl> getComic_list() {
        return comic_list;
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

    public void cacheBook(int progress) {
        cacheBook(progress + 1, false);
    }

    /**
     * 缓存下一章节
     *
     * @param progress 开始缓存位置
     * @param bool     是否缓存所有章节
     */
    public void cacheBook(int progress, boolean bool) {
        progress = progress > -1 ? progress : this.progress;
        uuid = UUID.randomUUID().toString();
        String url = Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress));
        int finalProgress = progress;
        if (url == null) return;
        rule.bookChapters(book, url, (data, label) -> {
            if (!data.isStatus()){
                cache_error++;
                //失败三次取消缓存
                if (cache_error < 3) {
                    cacheBook(finalProgress, bool);
                } else {
                    cache_error = 0;
                }
            } else if (bool) {
                if (isHaveNextChapters(finalProgress)) {
                    cacheBook(finalProgress + 1, true);
                } else {
                    //全部缓存完成事件
                    NotificationUtil.sendMessage("《" + book.getTitle() + "》缓存完成");
                }
            }
        }, uuid);
    }

    public boolean isHaveNextChapters() {
        if (isComic()) {
            return isHaveNextChapters(current_progress_page(start)[0]);
        }
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
        if (isComic()) {
            return isHavePreviousChapters(current_progress_page(start)[0]);
        }
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
        if (isComic()) {
            getContentComic(false);
        } else {
            getContent();
        }
    }

    public void loadPreviousChapters() {
        progress = _havePreviousChapters(progress);
        if (isComic()) {
            getContentComic(false);
        } else {
            getContent(true);
        }
    }

    /**
     * 章节转跳
     *
     * @param pos 转跳位置
     */
    public void jumpChapters(int pos) {
        start = 0;
        progress = pos;
        comic_list.clear();
        comic_chapters.clear();
        saveProgress(progress);
        if (isComic()) {
            getContentComic(true);
        } else {
            getContent();
        }
    }

    /**
     * 同于获取实际位置
     *
     * @param current_page 当前位置
     * @return 实际位置
     */
    public int[] current_progress_page(int current_page) {
        current_page++;
        int[] pages = new int[2];
        int index = 0;
        // 当前总页数减去每章页数，得到当前的页数的位置
        for (; index < comic_chapters.size(); index++) {
            if (current_page > comic_chapters.get(index)) {
                current_page = current_page - comic_chapters.get(index);
            } else {
                break;
            }
        }

        //计算章节中间的卷数量（卷指 1-50 章节的标题）
        int temp = 0;
        for (int i = 0; i <= index; i++) {
            while (isSubtitle(i + temp)) {
                temp++;
            }
        }
        //最开始位置 + 卷 + 章节 = 实际位置
        pages[0] = (progress - Math.max(comic_chapters.size(),1) + 1) + index + temp;

        pages[1] = current_page - 1;
        return pages;
    }

    public boolean isSubtitle(int progress) {
        return Objects.requireNonNull(data_catalogue.getValue()).get(catalogue.get(progress)) == null;
    }

    public boolean isComic() {
        return rule != null && rule.getAnalysis().isComic();
    }

    public MutableLiveData<LinkedHashMap<String, String>> getDataCatalogue() {
        return data_catalogue;
    }

    public MutableLiveData<HashMap<String, Object>> getDataContent() {
        return data_content;
    }

    public MutableLiveData<String> getChapters() {
        return chapters;
    }

    public void setFont_setting(String s){
        font_setting.postValue(s);
    }

    public MutableLiveData<String> getFont_setting() {
        return font_setting;
    }
}