package com.zhhz.reader.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class BookBean implements Serializable {

    private String book_id;
    private String title;
    private String author;
    private String cover;
    private String categories;
    private String catalogue;
    private String latestChapter;
    private String update_time;
    private String intro;
    //是否完结
    private boolean status;

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

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getCatalogue() {
        return catalogue;
    }

    public void setCatalogue(String catalogue) {
        this.catalogue = catalogue;
    }

    public String getLatestChapter() {
        return latestChapter;
    }

    public void setLatestChapter(String latestChapter) {
        this.latestChapter = latestChapter;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdateTime(String update_time) {
        this.update_time = update_time;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public boolean isStatus() {
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
                ", categories='" + categories + '\'' +
                ", catalogue='" + catalogue + '\'' +
                ", latestChapter='" + latestChapter + '\'' +
                ", update_time='" + update_time + '\'' +
                ", intro='" + intro + '\'' +
                ", status=" + status +
                '}';
    }
}
