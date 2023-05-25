package com.zhhz.reader.bean.rule;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.annotation.JSONType;

@JSONType(orders={"name","author","cover","intro","status","updateTime","lastChapter","updateTime","catalog"})
public class Detail {

    private String name;
    private String author;
    private String intro;
    private String status;
    private String updateTime;
    private String lastChapter;
    private String cover;
    private String catalog;

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

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIntro() {
        return intro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setLastChapter(String lastchapter) {
        this.lastChapter = lastchapter;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getCatalog() {
        return catalog;
    }

    @NonNull
    @Override
    public String toString() {
        return "Detail{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", summary='" + intro + '\'' +
                ", status='" + status + '\'' +
                ", update='" + updateTime + '\'' +
                ", lastchapter='" + lastChapter + '\'' +
                ", cover='" + cover + '\'' +
                ", catalog='" + catalog + '\'' +
                '}';
    }
}