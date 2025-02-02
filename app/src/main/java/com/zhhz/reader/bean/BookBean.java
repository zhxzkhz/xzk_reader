package com.zhhz.reader.bean;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class BookBean implements Serializable, Parcelable {

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

    public BookBean(){}
    protected BookBean(Parcel in) {
        book_id = in.readString();
        title = in.readString();
        author = in.readString();
        cover = in.readString();
        catalogue = in.readString();
        lastChapter = in.readString();
        update = in.readByte() != 0;
        updateTime = in.readString();
        intro = in.readString();
        status = in.readByte() != 0;
        comic = in.readByte() != 0;
    }

    public static final Creator<BookBean> CREATOR = new Creator<BookBean>() {
        @Override
        public BookBean createFromParcel(Parcel in) {
            return new BookBean(in);
        }

        @Override
        public BookBean[] newArray(int size) {
            return new BookBean[size];
        }
    };

    public String getBookId() {
        return book_id;
    }

    public void setBookId(String book_id) {
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
                ", catalogue='" + catalogue + '\'' +
                ", lastChapter='" + lastChapter + '\'' +
                ", update=" + update +
                ", updateTime='" + updateTime + '\'' +
                ", intro='" + intro + '\'' +
                ", status=" + status +
                ", comic=" + comic +
                '}';
    }

    public boolean isComic() {
        return comic;
    }

    public BookBean setComic(boolean comic) {
        this.comic = comic;
        return this;
    }

    @Override
    public int describeContents() {
        System.out.println("\"describeContents\" = " + "describeContents");
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(book_id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(cover);
        dest.writeString(catalogue);
        dest.writeString(lastChapter);
        dest.writeByte((byte) (update?1:0));
        dest.writeString(updateTime);
        dest.writeString(intro);
        dest.writeByte((byte) (status?1:0));
        dest.writeByte((byte) (comic?1:0));
    }
}
