package com.zhhz.reader.bean.rule;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.annotation.JSONType;

import cn.hutool.core.util.ObjectUtil;

@JSONType(orders = {"url", "list", "charset", "name", "author", "cover", "lastChapter", "intro", "detail"})
public class Search {

    private String url;
    private String charset;
    private String list;
    private String name;
    private String author;
    private String cover;
    private String lastChapter;
    private String intro;
    private String detail;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getList() {
        return list;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    @NonNull
    @Override
    public String toString() {
        return "Search{" +
                "url='" + url + '\'' +
                ", charset='" + charset + '\'' +
                ", list='" + list + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", cover='" + cover + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}