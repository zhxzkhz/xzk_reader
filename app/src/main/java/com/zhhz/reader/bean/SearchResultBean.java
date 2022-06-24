package com.zhhz.reader.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class SearchResultBean implements Serializable {
    private String name;
    private List<String> source;
    private String title;
    private String url;
    private String author;
    private String cover;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    @NonNull
    @Override
    public String toString() {
        return "SearchResultBean{" +
                "name='" + name + '\'' +
                ", source=" + source +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", author='" + author + '\'' +
                ", cover='" + cover + '\'' +
                '}';
    }
}
