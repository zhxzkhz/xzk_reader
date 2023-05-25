package com.zhhz.reader.bean.rule;
import androidx.annotation.NonNull;

import com.alibaba.fastjson2.annotation.JSONType;

import java.util.List;

@JSONType(orders={"content","js","page","filter","purify"})
public class Chapter {

    private List<String> filter;
    private List<String> purify;
    private String page;
    private String content;
    private String js;
    public void setFilter(List<String> filter) {
         this.filter = filter;
     }
     public List<String> getFilter() {
         return filter;
     }

    public void setPurify(List<String> purify) {
         this.purify = purify;
     }
     public List<String> getPurify() {
         return purify;
     }

    public void setPage(String page) {
         this.page = page;
     }
     public String getPage() {
         return page;
     }

    public void setContent(String content) {
         this.content = content;
     }
     public String getContent() {
         return content;
     }

    public void setJs(String js) {
         this.js = js;
     }
     public String getJs() {
         return js;
     }

    @NonNull
    @Override
    public String toString() {
        return "Chapter{" +
                "filter=" + filter +
                ", purify=" + purify +
                ", page='" + page + '\'' +
                ", content='" + content + '\'' +
                ", js='" + js + '\'' +
                '}';
    }
}