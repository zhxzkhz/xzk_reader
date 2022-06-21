package com.zhhz.reader.rule;

import static com.zhhz.reader.rule.RuleAnalysis.client;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.zhhz.lua.LuaVirtual;
import com.zhhz.reader.util.Auto_Base64;
import com.zhhz.reader.util.DiskCache;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JsoupAnalysis {

    public static String save_path = null;

    private JSONObject json;
    //规则网站地址
    private String url;
    //规则名字
    private String name;
    //是否属于漫画
    private boolean comic;
    //规则使用文本编码
    private String charset;
    //用于标记 响应协议；
    private String http;
    //特殊表
    private HashMap<String, String> replace_map;
    //书本信息
    private JSONObject detail;

    private static JSONObject readText(String path) throws IOException {
        File file = new File(path);
        if (!file.isFile()) throw new FileNotFoundException("文件未找到");
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

    public JsoupAnalysis(String path) throws IOException, ClassNotFoundException {
        this(readText(path));
    }

    public JsoupAnalysis(JSONObject jsonObject) throws IOException, ClassNotFoundException {
        this.json = jsonObject;
        this.url = jsonObject.getString("url");
        this.name = jsonObject.getString("name");
        this.comic = (boolean) jsonObject.getBooleanValue("comic");
        this.charset = (String) (jsonObject.getJSONObject("search").getOrDefault("charset", "utf8"));
        if (this.charset == null) this.charset = "utf8";
        this.http = jsonObject.getJSONObject("search").getString("url").split(":")[0];
        replace_init();
    }

    private void replace_init() {
        replace_map = new HashMap<>();
        replace_map.put("<p>", "");
        replace_map.put("</p>", "");
        replace_map.put("&nbsp;", " ");
        replace_map.put("&lt;", "<");
        replace_map.put("&gt;", ">");
        replace_map.put("<br><br>", "\n");
        replace_map.put("<br>\n<br>", "\n");
        replace_map.put("\n\n", "\n");
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

    public HashMap<String, String> getReplace_map() {
        return replace_map;
    }

    public void setReplace_map(HashMap<String, String> replace_map) {
        this.replace_map = replace_map;
    }

    public JSONObject getDetail() {
        return detail;
    }

    public void setDetail(JSONObject detail) {
        this.detail = detail;
    }

    public void setDetail(String detail) {
        this.detail = JSONObject.parseObject(detail);
    }

    private String[] parse_array(String s) {
        String[] arr = new String[2];
        int index = s.indexOf('@');
        if (index > -1) {
            arr[0] = s.substring(0, index);
            arr[1] = s.substring(index + 1);
        } else {
            arr[0] = s;
        }
        return arr;
    }

    private String parse_jsoup(Object s, String reg) {
        if (reg == null) {
            if (s instanceof String) {
                return s.toString();
            }
            return ((Elements) s).text();
        }

        if (detail != null && reg.contains("$")) {
            reg = reg.replace("${title}", (CharSequence) Objects.requireNonNull(detail.getOrDefault("书名", ""))).replace("${author}", (CharSequence) Objects.requireNonNull(detail.getOrDefault("作者", "")));
        }

        String x_reg = "([^-]*)->(.*)";
        Pattern mailPattern = Pattern.compile(x_reg);

        //优化方法，支持多次使用方法
        String[] req_arr = reg.split("@");
        for (String reg_x : req_arr) {
            Matcher matcher = mailPattern.matcher(reg_x);
            if (!matcher.find()) {
                s = ((Elements) s).text();
            }
            String k = matcher.group(1);
            String v = matcher.group(2);
            if (v == null) v = "";
            assert k != null;
            if (!k.equals("attr") && !(s instanceof String)) {
                s = ((Elements) s).text();
            }

            switch (k) {
                case "attr":
                    s = ((Elements) s).attr(v);
                case "match": {
                    Pattern p = Pattern.compile(v);
                    Matcher m = p.matcher((String) s);
                    if (m.find()) {
                        s = m.group();
                    }
                    break;
                }
                case "replace": {
                    Matcher m = mailPattern.matcher(v);
                    if (m.find()) {
                        s = ((String) s).replace(Objects.requireNonNull(m.group(1)), Objects.requireNonNull(m.group(2)));
                    }
                    break;
                }
            }
        }


        return String.valueOf(s);
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

    private boolean isNil(Object o, CallBack callback) {
        if (o == null) {
            callback.run(null, null, null);
            return false;
        }
        return true;
    }

    public void BookSearch(@NonNull JsoupAnalysis jsoupAnalysis, String key_word, CallBack callback, int index) {
        jsoupAnalysis.BookSearch(key_word, callback, index);
    }

    public void BookSearch(String key_word, CallBack callback, int index) {
        if (json.getOrDefault("encode", null) != null) {
            try {
                key_word = URLEncoder.encode(key_word, this.charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String url = json.getJSONObject("search").getString("url").replace("${key}", key_word);
        String page = (String) json.getJSONObject("search").getOrDefault("page", null);
        if (page != null) {
            url = url.replace("${page}", page);
        }
        final String url_s = url;
        Http(url, (a, b, c) -> {
            if (a == null) {
                callback.run(null, b, c);
                return;
            }

            Element data = (Element) a;
            List<HashMap<String, Object>> al = new ArrayList<>();
            JSONObject search = json.getJSONObject("search");

            Elements list = data.select(search.getString("list"));
            for (Element dl : list) {
                HashMap<String, Object> hm = new HashMap<>();
                hm.put("来源", name);
                List<Integer> source = new ArrayList<>();
                source.add(index);
                hm.put("rule", source);

                String[] rule = parse_array(search.getString("name"));
                hm.put("书名", parse_jsoup(dl.select(rule[0]), rule[1]));

                String[] detail = parse_array(search.getString("detail"));
                hm.put("地址", to_http(parse_jsoup(dl.select(detail[0]), detail[1] != null ? detail[1] : "attr->href"), url_s));

                if (search.get("author") != null) {
                    String[] author = parse_array(search.getString("author"));
                    hm.put("作者", parse_jsoup(dl.select(author[0]), author[1] != null ? author[1] : null));
                }

                if (search.get("cover") != null) {
                    String[] cover = parse_array(search.getString("cover"));
                    hm.put("封面", to_http(parse_jsoup(dl.select(cover[0]), cover[1] != null ? cover[1] : "attr->src"), url_s));
                }

                al.add(hm);
            }

            callback.run(al, null, null);
        });
    }

    public void BookDirectory(@NonNull JsoupAnalysis jsoupAnalysis, String url, CallBack callback) {
        jsoupAnalysis.BookDirectory(url, callback);
    }

    public void BookDirectory(String url, CallBack callback) {

        Http(url, (a, b, c) -> {
            if (a == null) {
                callback.run(null, b, c);
                return;
            }
            JSONObject catalog = json.getJSONObject("catalog");
            Element data = (Element) a;
            if (catalog.getString("lua") != null) {
                lua_callback.callBack = callback;
                LuaVirtual.newInstance().doString(Auto_Base64.decodeToString(catalog.getString("lua")),url, data, lua_callback.class, JsoupAnalysis.this);
                return;
            }


            LinkedHashMap<String, Object> lhm = CatalogAnalysis(url, data);

            if (catalog.getString("page") != null && catalog.getString("page").length() > 0) {
                String[] tmp = parse_array(catalog.getString("page"));
                String page = to_http(parse_jsoup(data.select(tmp[0]), tmp[1] != null ? tmp[1] : "attr->href"), url);
                if (page.length() > 0 && !page.equals(url)) {
                    BookDirectory(page, (a1, b1, c1) -> {
                        if (a1 != null) {
                            lhm.putAll((LinkedHashMap<String, Object>) a1);
                            callback.run(lhm, b1, c1);
                        } else {
                            callback.run(null, b1, c1);
                        }
                    });
                } else {
                    callback.run(lhm, url, c);
                }
            } else {
                callback.run(lhm, url, c);
            }
        });
    }

    public void BookDetail(@NonNull JsoupAnalysis jsoupAnalysis, String url, CallBack callback) {
        jsoupAnalysis.BookDetail(url, callback);
    }

    public void BookDetail(String url, CallBack callback) {
        Http(url, (a, b, c) -> {
            if (a == null) {
                callback.run(null, b, c);
                return;
            }
            Element data = (Element) a;
            HashMap<String, Object> map = new HashMap<>();
            JSONObject detail_x = json.getJSONObject("detail");
            if (detail_x.get("name") != null) {
                String[] obj = parse_array(detail_x.getString("name"));
                map.put("书名", parse_jsoup(data.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.get("author") != null) {
                String[] obj = parse_array(detail_x.getString("author"));
                map.put("作者", parse_jsoup(data.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.get("summary") != null) {
                String[] obj = parse_array(detail_x.getString("summary"));
                map.put("简介", parse_jsoup(data.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.get("cover") != null) {
                String[] obj = parse_array(detail_x.getString("cover"));
                map.put("封面", to_http(parse_jsoup(data.select(obj[0]), obj[1] != null ? obj[1] : "attr->src"), url));
            }
            if (detail_x.get("update") != null) {
                String[] obj = parse_array(detail_x.getString("update"));
                map.put("更新时间", parse_jsoup(data.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.get("lastChapter") != null) {
                String[] obj = parse_array(detail_x.getString("lastChapter"));
                map.put("最新章节", parse_jsoup(data.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.getString("catalog") == null) {
                map.put("目录地址", url);
                if (json.getJSONObject("catalog").get("page") == null && json.getJSONObject("catalog").getString("lua") == null) {
                    map.put("目录", CatalogAnalysis(url, data));
                }
            } else if (detail_x.getString("catalog").startsWith("true@")) {
                String x_url = (String) LuaVirtual.newInstance().doString(Auto_Base64.decodeToString(detail_x.getString("catalog").substring(5)),data, JsoupAnalysis.this);
                map.put("目录地址", x_url);
            } else {
                String[] obj = parse_array(detail_x.getString("catalog"));
                String str = to_http(parse_jsoup(data.select(obj[0]), obj[1] != null ? obj[1] : "attr->href"), url);
                if (str.equals(url) && json.getJSONObject("catalog").getString("page") == null && json.getJSONObject("catalog").getString("lua") == null) {
                    map.put("目录", CatalogAnalysis(url, data));
                }
                map.put("目录地址", str);
            }
            detail = new JSONObject(map);
            callback.run(map, null, null);
        });
    }

    public LinkedHashMap<String, Object> CatalogAnalysis(@NonNull JsoupAnalysis jsoupAnalysis, String url, Element data) {
        return jsoupAnalysis.CatalogAnalysis(url, data);
    }

    public LinkedHashMap<String, Object> CatalogAnalysis(String url, Element data) {
        LinkedHashMap<String, Object> lhm = new LinkedHashMap<>();
        String[] booklet_name = null;
        Elements list;
        JSONObject catalog = json.getJSONObject("catalog");
        JSONObject booklet = catalog.getJSONObject("booklet");

        if (booklet != null && booklet.get("list") != null && booklet.get("name") != null) {
            booklet_name = parse_array(booklet.getString("name"));
            //查询卷名和章节名
            list = data.select(booklet.getString("list") + " , " + catalog.getString("list"));
            // 倒序 ，并且 标题 和 章节不是一个标签才支持倒序
            if (catalog.getBoolean("inverted") && !catalog.getString("name").equals(booklet.getString("name"))) {
                Elements list_b = data.select(booklet.getString("list"));
                ArrayList<Integer> list_type = new ArrayList<>();
                HashMap<Element, Integer> map = new HashMap<>();
                for (int i = 0; i < list.size(); i++) {
                    map.put(list.get(i), i);
                }
                for (Element element : list_b) {
                    list_type.add(map.get(element));
                }

                list.sort((a, b) -> {
                    if (list_b.contains(a)) {
                        if (list_b.contains(b)) {
                            return 0;
                        }
                        //固定范围标签位置
                        if (list_b.indexOf(a) > JsoupAnalysis.this.exist_scope(list_type, map.get(b))) {
                            return 1;
                        }
                        return -1;
                    } else if (!list_b.contains(b)) {
                        // 判断一个范围的进行倒序
                        if (JsoupAnalysis.this.exist_scope(list_type, map.get(b)) == JsoupAnalysis.this.exist_scope(list_type, map.get(a))) {
                            if (map.get(a) > map.get(b)) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    } else {
                        //固定范围标签位置
                        if (list_b.indexOf(b) == JsoupAnalysis.this.exist_scope(list_type, map.get(a))) {
                            return 1;
                        }
                    }
                    return map.get(a) - map.get(b);

                });
                map.clear();
                list_b.clear();
                list_type.clear();
            }
        } else {
            list = data.select(catalog.getString("list"));
            //倒序
            if (catalog.get("inverted") != null && catalog.getBoolean("inverted")) {
                Collections.reverse(list);
            }
        }

        for (Element element : list) {
            Elements booklet_names = null;
            if (booklet_name != null) {
                booklet_names = element.select(booklet_name[0]);
            }
            if (booklet_names != null && booklet_names.size() > 0) {
                lhm.put(parse_jsoup(booklet_names, booklet_name[1]), false);
            } else {
                String[] chapter = parse_array(catalog.getString("chapter"));
                String a_url = parse_jsoup(element.select(chapter[0]), chapter[1] != null ? chapter[1] : "attr->href");
                if (a_url.length() > 0) {
                    String[] obj = parse_array(catalog.getString("name"));
                    lhm.put(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : null), to_http(a_url, url));
                }
            }
        }
        return lhm;
    }

    /**
     * 排序函数，用于返回章节存在的区间
     *
     * @param index 排序前位置
     * @return 排序后位置
     */
    private int exist_scope(ArrayList<Integer> list_type, int index) {
        int a = -1;
        for (int i = 0; i < list_type.size(); i++) {
            if (index > list_type.get(i)) {
                a = i;
            }
        }
        return a;
    }

    public void Chapters(@NonNull JsoupAnalysis jsoupAnalysis, String url, CallBack callback, Object random) {
        jsoupAnalysis.Chapters(url, callback, random);
    }

    public void Chapters(String url, CallBack callback, Object random) {
        File file = new File(save_path + "/book_chapter/" + url.substring(url.lastIndexOf('/') + 1));

        if (!Objects.requireNonNull(file.getParentFile()).isDirectory()) {
            if (file.getParentFile().mkdirs()){
                Log.d("Chapters","创建成功");
            }
        }
        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);) {
                int size = fis.available();
                byte[] bytes = new byte[size];
                if (fis.read(bytes) == size) {
                    callback.run(new String(bytes), random, null);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BookContent(url, (a, b, c) -> {
            if (a == null) {
                callback.run(null, b, c);
                return;
            }
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                fos.write(((String) a).getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            callback.run(a, b, c);
        }, random);

    }

    public void BookContent(@NonNull JsoupAnalysis jsoupAnalysis, String url, CallBack callback, Object random) {
        jsoupAnalysis.BookContent(url, callback, random);
    }

    public void BookContent(String url, CallBack callback, Object random) {

        Http_Get(url, (a, b, c) -> {
            if (a == null) {
                callback.run(null, b, c);
                return;
            }
            ((Document) a).outputSettings().prettyPrint(false);
            JSONObject chapter = json.getJSONObject("chapter");
            String[] content_x = parse_array(chapter.getString("content"));
            Element data = (Element) a;
            Elements content = data.select(content_x[0]);
            StringBuilder filter_str = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            if (chapter.get("filter") != null) {
                for (Object filter : chapter.getJSONArray("filter")) {
                    if (((String) filter).startsWith("@")) {
                        filter_str.append(',').append(((String) filter).substring(1));
                    }
                }
                Elements content1;
                if (filter_str.length() == 0) {
                    content1 = content.select(content_x[0]);
                } else {
                    content1 = content.select(content_x[0] + ">" + filter_str.substring(1));
                }

                for (Element element : content1) {
                    element.remove();
                }

                if (content.size() == 1) {
                    content.html(content.html());
                }
            }

            if (comic) {
                sb.append(content.html());
            } else {
                for (TextNode textNode : content.textNodes()) {
                    sb.append("\n");
                    sb.append(textNode.text().trim());
                }
            }

            String str;

            //屏蔽规则
            if (chapter.get("purify") != null) {
                str = sb.toString();
                for (Object purify : chapter.getJSONArray("purify")) {
                    str = str.replace((CharSequence) purify, "");
                }
            } else {
                str = sb.toString();
            }

            if (!str.startsWith("http://")) {
                for (Map.Entry<String, String> entry : replace_map.entrySet()) {
                    str = str.replace(entry.getKey(), entry.getValue());
                }
            }

            str = str.replaceAll("^\n*|\n*$", "");

            //执行lua
            if (chapter.getString("lua") != null) {
                str = (String) LuaVirtual.newInstance().doString(Auto_Base64.decodeToString(chapter.getString("lua")),str, JsoupAnalysis.this,url,callback,random,data);
                if (str!=null && str.equals("false")){
                    return;
                }
            }

            if (chapter.getString("page") != null) {
                String[] tmp = parse_array(chapter.getString("page"));
                String page = to_http(parse_jsoup(data.select(tmp[0]), tmp[1] != null ? tmp[1] : "attr->href"), url);
                if (page.length() > 0 && !page.equals(url)) {
                    String finalStr = str;
                    BookContent(page, (a1, b1, c1) -> {
                        if (a1 == null) {
                            callback.run(null, b1, c1);
                        } else {
                            callback.run(finalStr + a1, b1, c1);
                        }
                    }, random);
                } else {
                    callback.run(str, random, b);
                }
            } else {
                callback.run(str, random, b);
            }
        });
    }


    public void Http(JsoupAnalysis o, String data, CallBack callBack) {
        o.Http(data, callBack);
    }

    public void Http(String data, CallBack callBack) {
        if (data.contains("@post->")) {
            Http_Post(data, callBack);
        } else {
            Http_Get(data, callBack);
        }
    }


    public void Http_Post(JsoupAnalysis p, String url, CallBack callback) {
        p.Http_Post(url, callback);
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

    public void Http_Get(JsoupAnalysis o, String url, CallBack callback) {
        o.Http_Get(url, callback);
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
            JSONObject header = JSONObject.parseObject(this.json.getString("header"));
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
                    MediaType contentType = Objects.requireNonNull(response.body()).contentType();
                    String charset = null;
                    if (contentType != null && contentType.charset() != null) {
                        charset = Objects.requireNonNull(contentType.charset()).name();
                    }
                    if (charset == null) {
                        charset = JsoupAnalysis.this.charset;
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

    @FunctionalInterface
    public interface CallBack {
        /**
         * @param a 数据
         * @param b 报错提示
         * @param c 未知
         */
        void run(Object a, Object b, Object c);

    }

    public static class lua_callback implements Serializable {
        public static CallBack callBack;

        public lua_callback(Object a) {
            callBack.run(a, null, null);
        }

        public lua_callback(Object a, Object b) {
            callBack.run(a, b, null);
        }

        public lua_callback(Object a, Object b, Object c) {
            callBack.run(a, b, c);
        }
    }

}
