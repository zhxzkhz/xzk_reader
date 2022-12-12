package com.zhhz.reader.bean.rule;

import com.alibaba.fastjson2.annotation.JSONType;

@JSONType(orders={"list","name","chapter","js","inverted","booklet","page"})
public class Catalog {

    private String list;
    private String name;
    private String chapter;
    private boolean inverted;
    private String js;
    private String page;
    private Booklet booklet;
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

    public void setChapter(String chapter) {
         this.chapter = chapter;
     }
     public String getChapter() {
         return chapter;
     }

    public void setInverted(boolean inverted) {
         this.inverted = inverted;
     }
     public boolean getInverted() {
         return inverted;
     }
    public void setJs(String js) {
        this.js = js;
    }

    public String getJs() {
        return js;
    }
    public void setPage(String page) {
         this.page = page;
     }
     public String getPage() {
         return page;
     }

    public void setBooklet(Booklet booklet) {
         this.booklet = booklet;
     }
     public Booklet getBooklet() {
         return booklet;
     }

    @Override
    public String toString() {
        return "Catalog{" +
                "list='" + list + '\'' +
                ", name='" + name + '\'' +
                ", chapter='" + chapter + '\'' +
                ", inverted=" + inverted +
                ", js='" + js + '\'' +
                ", page='" + page + '\'' +
                ", booklet=" + booklet +
                '}';
    }
}