package com.zhhz.reader.rule;

import com.alibaba.fastjson.JSONObject;
import com.zhhz.lua.LuaVirtual;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.Auto_Base64;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupAnalysis extends Analysis {

    //特殊表
    private HashMap<String, String> replace_map;
    //书本信息
    private JSONObject detail;

    public JsoupAnalysis(String path) throws IOException {
        super(path);
        replace_init();
    }

    public JsoupAnalysis(JSONObject jsonObject) {
        super(jsonObject);
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
            reg = reg.replace("${title}", (CharSequence) Objects.requireNonNull(detail.getOrDefault("title", ""))).replace("${author}", (CharSequence) Objects.requireNonNull(detail.getOrDefault("author", "")));
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

    @Override
    public void BookSearch(String key_word, CallBack callback, String md5) {
        if (json.getString("encode") != null) {
            try {
                key_word = URLEncoder.encode(key_word, this.charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String url = json.getJSONObject("search").getString("url").replace("${key}", key_word);
        String page = json.getJSONObject("search").getString("page");
        if (page != null) {
            url = url.replace("${page}", page);
        }
        final String url_s = url;
        Http(url, (data, msg, label) -> {
            if (data == null) {
                callback.run(null, msg, label);
                return;
            }

            Element element = (Element) data;
            List<SearchResultBean> al = new ArrayList<>();
            JSONObject search = json.getJSONObject("search");

            Elements list = element.select(search.getString("list"));
            for (Element dl : list) {
                SearchResultBean SearchResultBean = new SearchResultBean();
                SearchResultBean.setName(name);
                List<String> source = new ArrayList<>();
                source.add(md5);
                SearchResultBean.setSource(source);
                String[] rule = parse_array(search.getString("name"));
                SearchResultBean.setTitle(parse_jsoup(dl.select(rule[0]), rule[1]));

                String[] detail = parse_array(search.getString("detail"));
                SearchResultBean.setUrl(to_http(parse_jsoup(dl.select(detail[0]), detail[1] != null ? detail[1] : "attr->href"), url_s));

                if (search.get("author") != null) {
                    String[] author = parse_array(search.getString("author"));
                    SearchResultBean.setAuthor(parse_jsoup(dl.select(author[0]), author[1] != null ? author[1] : null));
                }

                if (search.get("cover") != null) {
                    String[] cover = parse_array(search.getString("cover"));
                    SearchResultBean.setCover(to_http(parse_jsoup(dl.select(cover[0]), cover[1] != null ? cover[1] : "attr->src"), url_s));
                }

                al.add(SearchResultBean);
            }
            callback.run(al, null, null);
        });
    }

    @Override
    public void BookDetail(String url, CallBack callback) {
        Http(url, (data, msg, label) -> {
            if (data == null) {
                callback.run(null, msg, label);
                return;
            }
            Element element = (Element) data;
            BookBean book = new BookBean();
            JSONObject detail_x = json.getJSONObject("detail");
            if (detail_x.get("name") != null) {
                String[] obj = parse_array(detail_x.getString("name"));
                book.setTitle(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.get("author") != null) {
                String[] obj = parse_array(detail_x.getString("author"));
                book.setAuthor(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.get("summary") != null) {
                String[] obj = parse_array(detail_x.getString("summary"));
                book.setIntro(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.get("cover") != null) {
                String[] obj = parse_array(detail_x.getString("cover"));
                book.setCover(to_http(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : "attr->src"), url));
            }
            if (detail_x.get("update") != null) {
                String[] obj = parse_array(detail_x.getString("update"));
                book.setUpdateTime(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : null));
            }

            if (detail_x.get("status") != null) {
                String[] obj = parse_array(detail_x.getString("status"));
                book.setStatus(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : null).contains("完结"));
            }

            if (detail_x.get("lastChapter") != null) {
                String[] obj = parse_array(detail_x.getString("lastChapter"));
                book.setLatestChapter(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : null));
            }
            if (detail_x.getString("catalog") == null) {
                book.setCatalogue(url);
            } else if (detail_x.getString("catalog").startsWith("true@")) {
                String x_url = (String) LuaVirtual.newInstance().doString(Auto_Base64.decodeToString(detail_x.getString("catalog").substring(5)), element, JsoupAnalysis.this);
                book.setCatalogue(x_url);
            } else {
                String[] obj = parse_array(detail_x.getString("catalog"));
                String str = to_http(parse_jsoup(element.select(obj[0]), obj[1] != null ? obj[1] : "attr->href"), url);
                book.setCatalogue(str);
            }

            callback.run(book, null, null);
        });
    }

    @Override
    public void BookDirectory(String url, CallBack callback) {

        Http(url, (data, msg, label) -> {
            if (data == null) {
                callback.run(null, msg, label);
                return;
            }
            JSONObject catalog = json.getJSONObject("catalog");
            Element element = (Element) data;

            if (catalog.getString("lua") != null) {
                LuaVirtual.newInstance().doString(Auto_Base64.decodeToString(catalog.getString("lua")), url, element, callback, JsoupAnalysis.this);
                return;
            }

            LinkedHashMap<String, String> lhm = CatalogAnalysis(url, element);

            if (catalog.getString("page") != null && catalog.getString("page").length() > 0) {
                String[] tmp = parse_array(catalog.getString("page"));
                String page = to_http(parse_jsoup(element.select(tmp[0]), tmp[1] != null ? tmp[1] : "attr->href"), url);
                if (page.length() > 0 && !page.equals(url)) {
                    BookDirectory(page, (data_a, msg_a, label_a) -> {
                        if (data_a != null) {
                            lhm.putAll((LinkedHashMap<String, String>) data_a);
                            callback.run(lhm, msg_a, label_a);
                        } else {
                            callback.run(null, msg_a, label_a);
                        }
                    });
                } else {
                    callback.run(lhm, url, label);
                }
            } else {
                callback.run(lhm, url, label);
            }
        });
    }

    public LinkedHashMap<String, String> CatalogAnalysis(String url, Element data) {
        LinkedHashMap<String, String> lhm = new LinkedHashMap<>();
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
                lhm.put(parse_jsoup(booklet_names, booklet_name[1]), null);
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

    @Override
    public void BookChapters(BookBean book, CallBack callback, Object random) {
        String url = book.getCatalogue();
        File file = new File(save_path + File.separator + book.getBook_id() + File.separator + "book_chapter" + File.separator + url.substring(url.lastIndexOf('/') + 1));

        if (!Objects.requireNonNull(file.getParentFile()).isDirectory()) {
            if (file.getParentFile().mkdirs()) {
                System.out.println("Chapters -> 创建成功");
            }
        }
        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
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

        System.out.println("开始请求");

        BookContent(url, (data, msg, label) -> {
            if (data == null) {
                callback.run(null, msg, label);
                return;
            }
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                fos.write(((String) data).getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            callback.run(data, msg, label);
        }, random);

    }

    @Override
    public void BookContent(String url, CallBack callback, Object label) {
        Http_Get(url, (data, msg, __) -> {
            if (data == null) {
                callback.run(null, msg, label);
                return;
            }
            ((Document) data).outputSettings().prettyPrint(false);
            JSONObject chapter = json.getJSONObject("chapter");
            String[] content_x = JsoupAnalysis.this.parse_array(chapter.getString("content"));
            Element element = (Element) data;
            Elements content = element.select(content_x[0]);
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

                for (Element ele : content1) {
                    ele.remove();
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

            String str = sb.toString();

            if (!JsoupAnalysis.this.isComic()) {
                for (Map.Entry<String, String> entry : replace_map.entrySet()) {
                    str = str.replace(entry.getKey(), entry.getValue());
                }
            }

            //屏蔽规则
            if (chapter.get("purify") != null) {
                for (Object purify : chapter.getJSONArray("purify")) {
                    str = str.replace((CharSequence) purify, "");
                }
            }

            str = str.replaceAll("^\n*|\n*$", "");

            //执行lua
            if (chapter.getString("lua") != null) {
                str = (String) LuaVirtual.newInstance().doString(Auto_Base64.decodeToString(chapter.getString("lua")), element, str, url, callback, label, JsoupAnalysis.this);
                //返回false代表 lua 内部处理
                if (str != null && str.equals("false")) {
                    return;
                }
            }

            if (chapter.getString("page") != null) {
                String[] tmp = JsoupAnalysis.this.parse_array(chapter.getString("page"));
                String page = JsoupAnalysis.this.to_http(JsoupAnalysis.this.parse_jsoup(element.select(tmp[0]), tmp[1] != null ? tmp[1] : "attr->href"), url);
                if (page.length() > 0 && !page.equals(url)) {
                    String finalStr = str;
                    JsoupAnalysis.this.BookContent(page, (data_a, msg_a, label_a) -> {
                        if (data_a == null) {
                            callback.run(null, msg_a, label_a);
                        } else {
                            callback.run(finalStr + data_a, msg_a, label_a);
                        }
                    }, label);
                } else {
                    callback.run(str, msg, label);
                }
            } else {
                callback.run(str, msg, label);
            }
        });
    }


}
