package com.zhhz.reader.bean.rule;

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

    @Override
    public String toString() {
        return "Booklet{" +
                "list='" + list + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}