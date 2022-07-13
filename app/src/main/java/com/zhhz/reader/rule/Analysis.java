package com.zhhz.reader.rule;

import static com.zhhz.reader.rule.RuleAnalysis.client;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.util.AutoBase64;
import com.zhhz.reader.util.DiskCache;

import org.jsoup.Jsoup;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import javax.script.ScriptException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class Analysis{

    public static String save_path = "D:\\星-阅读\\book";

    protected JSONObject json;
    //规则网站地址
    protected String url;
    //规则名字
    protected String name;
    //是否属于漫画
    protected boolean comic;
    //规则使用文本编码
    protected String charset;
    //用于标记 响应协议；
    protected String http;
    //书本信息
    protected BookBean detail;

    public Analysis(String path) throws IOException {
        this(readText(path));
    }

    public Analysis(JSONObject jsonObject) {
        this.json = jsonObject;
        this.url = jsonObject.getString("url");
        this.name = jsonObject.getString("name");
        this.comic = jsonObject.getBooleanValue("comic");
        this.charset = (String) (jsonObject.getJSONObject("search").getOrDefault("charset", "utf8"));
        if (this.charset == null) this.charset = "utf8";
        this.http = jsonObject.getJSONObject("search").getString("url").split(":")[0];
    }

    public static JSONObject readText(String path) throws IOException {
        File file = new File(path);
        if (!file.isFile()) throw new FileNotFoundException("文件未找到 -> " + file);
        FileInputStream fis;
        try {
            fis = new FileInputStream(path);
            int size = fis.available();
            byte[] bytes = new byte[size];
            if (fis.read(bytes) != size) throw new IOException("文件读取异常");
            fis.close();
            return JSONObject.parseObject(new String(bytes));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        throw new IOException("加载异常");
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getHttp() {
        return http;
    }

    public void setHttp(String http) {
        this.http = http;
    }

    public String to_http(String relative_path, String url) {
        if (relative_path.length() > 0 && !relative_path.startsWith("http")) {
            if (relative_path.startsWith("//")) {
                return http + ":" + relative_path;
            } else if (relative_path.startsWith("/")) {
                return http + "://" + this.url + relative_path;
            } else if (!relative_path.startsWith(".")) {
                return url.substring(0, url.lastIndexOf('/')) + "/" + relative_path;
            } else {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                String[] arr_url = url.split("/");
                String[] arr = relative_path.split("/");
                StringBuilder sb = new StringBuilder();
                int i = 0;
                int i1 = 3;
                for (String s : arr) {
                    if (s.equals(".")) {
                        sb.append(arr_url[2]);
                    } else if (s.equals("..")) {
                        i++;
                    } else {
                        if (i < arr_url.length - 2) {
                            for (; i1 < arr_url.length - i; i1++) {
                                sb.append('/');
                                sb.append(arr_url[2 + i1]);
                            }
                        }
                    }
                }
                return sb.toString();
            }
        }


        return relative_path;
    }

    public BookBean getDetail() {
        return detail;
    }

    public void setDetail(BookBean detail) {
        this.detail = detail;
    }

    public abstract void BookSearch(String key_word, CallBack callback, String md5);

    public abstract void BookDirectory(String url, CallBack callback);

    public abstract void BookDetail(String url, CallBack callback);

    public abstract void BookChapters(BookBean book, String url, CallBack callback, Object random);

    public abstract void BookContent(String url, CallBack callback, Object random);

    public void Http(String data, CallBack callBack) {
        Http(data, callBack,false);
    }

    public void Http(String data, CallBack callBack,boolean bool) {
        if (data.contains("@post->")) {
            Http_Post(data, callBack);
        } else {
            Http_Get(data, callBack);
        }
    }

    public void Http_Post(String url, CallBack callback) {
        String header = null, data;
        MediaType mt;
        String[] arr = url.split("@post->", 2);
        if (arr[1] != null && arr[1].contains("$header")) {
            String[] ar = arr[1].split("\\$header", 2);
            data = ar[0];
            header = ar[1];
        } else {
            data = arr[1];
        }

        assert data != null;
        if (data.startsWith("{")) {
            mt = MediaType.parse("application/json");
        } else {
            mt = MediaType.parse("application/x-www-form-urlencoded;charset=" + charset);
        }

        Request.Builder builder = new Request.Builder().url(arr[0]).post(RequestBody.create(mt, data));
        if (header != null) {
            JSONObject jsonObject = JSONObject.parseObject(header);
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        init_header(builder);
        newCall(builder.build(), callback);
    }

    public void Http_Get(String url, CallBack callback) {
        String header = null;
        if (url.contains("$header")) {
            String[] ar = url.split("\\$header", 2);
            url = ar[0];
            header = ar[1];
        }
        Request.Builder builder = new Request.Builder().url(url);
        init_header(builder);
        if (header != null) {
            JSONObject jsonObject = JSONObject.parseObject(header);
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        newCall(builder.build(), callback);
    }

    private void init_header(Request.Builder builder) {
        if (json.get("header") != null) {
            String h = this.json.getString("header");
            if (h.indexOf("js@") == 0) {
                try {
                    h = JsToJava(DiskCache.SCRIPT_ENGINE.eval(AutoBase64.decodeToString(h.substring(3))));
                } catch (ScriptException e) {
                    h = "{}";
                }
            }
            JSONObject header = JSONObject.parseObject(h);
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
            header.clear();
        }
    }

    private void newCall(Request request, CallBack callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.run(null, e.getMessage(), null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() == 200 && response.body() != null) {
                    MediaType contentType = response.body().contentType();
                    String charset = null;
                    if (contentType != null && contentType.charset() != null) {
                        charset = Objects.requireNonNull(contentType.charset()).name();
                    }
                    if (charset == null) {
                        charset = Analysis.this.charset;
                    }
                    String s = new String(Objects.requireNonNull(response.body()).bytes(), charset);
                    DiskCache.FileSave(DiskCache.path, call, s);
                    callback.run(Jsoup.parse(s), null, null);
                } else {
                    callback.run(null, response.message(), null);
                }
            }
        });
    }

    public String JsToJava(Object value) {
        if (value == null) return "";
        String str;
        if (value instanceof NativeArray) {
            NativeArray array = (NativeArray) value;
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : array) {
                stringBuilder.append(o).append("\n");
            }
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            str = stringBuilder.toString();
        } else if (value.getClass() == NativeJavaObject.class) {
            str = ((NativeJavaObject) value).unwrap().toString();
        } else {
            str = value.toString();
        }
        return str;
    }

    @FunctionalInterface
    public interface CallBack {
        /**
         * @param data  数据
         * @param msg   报错提示
         * @param label 随机数，用于标记
         */
        void run(Object data, Object msg, Object label);
    }
}
