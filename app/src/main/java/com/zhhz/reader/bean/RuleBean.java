package com.zhhz.reader.bean;

import androidx.annotation.NonNull;

public class RuleBean {
    private String id;
    private String name;
    private String file;
    private boolean comic;
    private boolean open;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComic() {
        return comic;
    }

    public void setComic(boolean comic) {
        this.comic = comic;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @NonNull
    @Override
    public String toString() {
        return "RuleBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", file='" + file + '\'' +
                ", comic=" + comic +
                ", open=" + open +
                '}';
    }
}
