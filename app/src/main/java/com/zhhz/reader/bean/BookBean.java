package com.zhhz.reader.bean;


import androidx.annotation.NonNull;

import java.io.Serializable;

public class BookBean implements Serializable {

    private String book_id;
    private String title;
    private String author;
    private String cover;
    private String catalogue;
    private String lastChapter;
    private boolean update;
    private String updateTime;
    private String intro;
    //是否完结
    private boolean status;

    private boolean comic;

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public boolean getUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getCatalogue() {
        return catalogue;
    }

    public void setCatalogue(String catalogue) {
        this.catalogue = catalogue;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String update_time) {
        this.updateTime = update_time;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "BookBean{" +
                "book_id='" + book_id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", cover='" + cover + '\'' +
                ", update='" + update + '\'' +
                ", catalogue='" + catalogue + '\'' +
                ", latestChapter='" + lastChapter + '\'' +
                ", update_time='" + updateTime + '\'' +
                ", intro='" + intro + '\'' +
                ", status=" + status +
                '}';
    }

    public boolean isComic() {
        return comic;
    }

    public BookBean setComic(boolean comic) {
        this.comic = comic;
        return this;
    }
}
