package com.zhhz.reader.bean.rule;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.annotation.JSONType;

@JSONType(orders={"list","name"})
public class Booklet {

    private String list;
    private String name;
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

    @NonNull
    @Override
    public String toString() {
        return "Booklet{" +
                "list='" + list + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}