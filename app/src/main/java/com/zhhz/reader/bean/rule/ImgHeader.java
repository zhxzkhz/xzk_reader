package com.zhhz.reader.bean.rule;

import com.alibaba.fastjson2.annotation.JSONType;

@JSONType(orders={"reuse","header"})
public class ImgHeader {

    private boolean reuse;
    private String header;
    public void setReuse(boolean reuse) {
         this.reuse = reuse;
     }
     public boolean getReuse() {
         return reuse;
     }

    public void setHeader(String header) {
         this.header = header;
     }
     public String getHeader() {
         return header;
     }

    @Override
    public String toString() {
        return "ImgHeader{" +
                "reuse=" + reuse +
                ", header='" + header + '\'' +
                '}';
    }
}