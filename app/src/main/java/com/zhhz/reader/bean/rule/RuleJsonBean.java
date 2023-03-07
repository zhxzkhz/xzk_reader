package com.zhhz.reader.bean.rule;

import com.alibaba.fastjson2.annotation.JSONType;

import cn.hutool.core.util.ObjectUtil;

@JSONType(orders={"name","version","url","encode","dns","type","header","comic","charset","imgHeader","search","detail","catalog","chapter"})
public class RuleJsonBean {

    private String name;
    private int version;
    private String url;
    private boolean encode;
    private String init;
    private int type;
    private String header;
    private String jsDecryption = "";
    private boolean comic;
    private boolean cache = true;
    private String charset;
    private boolean cookieJar;
    private ImgHeader imgHeader;
    private Search search;
    private Detail detail;
    private Catalog catalog;
    private Chapter chapter;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setEncode(boolean encode) {
        this.encode = encode;
    }

    public boolean getEncode() {
        return encode;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public String getInit() {
        return init;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public String getJsDecryption() {
        return jsDecryption;
    }

    public void setJsDecryption(String jsDecryption) {
        this.jsDecryption = jsDecryption;
    }

    public void setComic(boolean comic) {
        this.comic = comic;
    }

    public boolean getComic() {
        return comic;
    }

    public boolean getCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public String getCharset() {
        if (!ObjectUtil.isEmpty(charset)){
            return charset;
        }
        if (ObjectUtil.isNotNull(search)) {
            return search.getCharset();
        }
        return "utf8";
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setImgHeader(ImgHeader imgHeader) {
        this.imgHeader = imgHeader;
    }

    public ImgHeader getImgHeader() {
        return imgHeader;
    }

    public boolean getCookieJar() {
        return cookieJar;
    }

    public void setCookieJar(boolean cookieJar) {
        this.cookieJar = cookieJar;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public Search getSearch() {
        return search;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Chapter getChapter() {
        return chapter;
    }

    @Override
    public String toString() {
        return "{\n" +
                "  \"name\": " + (name == null ? "null" : "\"" + name + "\",\n") +
                "  \"version\": " + version + ",\n" +
                "  \"url\": " + (url == null ? "null" : "\"" + url + "\",\n") +
                "  \"encode\": " + encode + ",\n" +
                "  \"dns=\": " + (init == null ? "null" : "\"" + init + "\",\n") +
                "  \"type\": " + type + ",\n" +
                "  \"header\": " + (header == null ? "null" : "\"" + header + "\",\n") +
                "  \"comic\": " + comic + ",\n" +
                "  \"charset\": " + (charset == null ? "null" : "\"" + charset + "\",\n") +
                "  \"img_header\": " + (imgHeader == null ? "null" : imgHeader + ",\n") +
                "  \"search\": " + (search == null ? "null" : search + ",\n") +
                "  \"detail\": " + (detail == null ? "null" : detail + ",\n") +
                "  \"catalog\": " + (catalog == null ? "null" : catalog + ",\n") +
                "  \"chapter\": " + (chapter == null ? "null" : chapter + "\n") +
                '}';
    }
}