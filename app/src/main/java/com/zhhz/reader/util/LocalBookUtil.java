package com.zhhz.reader.util;

import android.net.Uri;

import com.alibaba.fastjson.JSONObject;
import com.zhhz.reader.MyApplication;
import com.zhhz.reader.bean.BookBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于解析本地书本
 */

public class LocalBookUtil {

    /**
     * 解析本地Txt文件
     */
    public static BookBean analysisBook(Uri uri) {

        //定义一个字符串用来储存读入的小说内容
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(MyApplication.context.getContentResolver().openInputStream(uri)))) {
            //从指定路径读取小说
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                if (s.isEmpty()) continue;
                stringBuilder.append(s.replaceAll("^[\u3000\u0020]*|[\u3000\u0020]*$", "")).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String name = uri.getPath().substring(uri.getPath().lastIndexOf("/")+1,uri.getPath().indexOf("."));

        BookBean bean = new BookBean();
        bean.setBook_id(StringUtil.getMD5(UUID.randomUUID().toString()));
        bean.setTitle(name);
        bean.setUpdate(false);
        bean.setStatus(true);

        if (analysisChapter(stringBuilder.toString(),bean.getBook_id())) {
            if (new File(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id()).delete()) return null;
            return null;
        }

        return bean;
    }

    /**
     * 解析章节
     * @param src 书本内容
     * @param book_id 书本id
     * @return 是否解析成功
     */
    private static boolean analysisChapter(String src,String book_id) {

        //匹配规则
        //[章节卷集部回]
        String pest = "(正文)?(第)([零〇一二三四五六七八九十百千万a-zA-Z\\d]{1,7})[章卷篇]((?! {4}).)((?!\\t{1,4}).){0,30}\\r?\\n";
        //content_list 用来储存章节内容
        List<String> content_list = new ArrayList<>();
        //chapter_list 用来储存章节名字
        List<String> chapter_list = new ArrayList<>();

        //根据匹配规则将小说分为一章一章的，并存到list
        for (String s : src.split(pest)) {
            content_list.add(s.replaceAll("\n{2,}",""));
        }

        //分割章节后的内容
        List<String> temp_content_list = new ArrayList<>();

        //默认添加一行简介，如果为空着不添加
        if (!content_list.get(0).isEmpty()) {
            chapter_list.add("简介");
            temp_content_list.add(content_list.get(0));
        }

        //java正则匹配
        Pattern p = Pattern.compile(pest);
        Matcher m = p.matcher(src);
        int i = 1;

        //临时字符
        String temp;
        //临时字符长度
        int temp_length;
        //临时下标，用于标记分割章节名字
        int temp_index;
        //每章最大字数(一章最多2w字，超过后分割)
        int content_max = 20000;
        //循环匹配
        while (m.find()) {
            //替换换行符
            String chapter = Objects.requireNonNull(m.group(0)).replace("\n", "");
            if (i == content_list.size())
                break;

            temp = content_list.get(i);
            //用于给分割章节命名 xxx_1
            temp_index = 1;

            //用于分割章节内容
            int count =(int) Math.ceil(temp.length() / (float) content_max);
            temp_length = temp.length() / count;
            while (temp_index <= count) {
                if (temp_index == 1) {
                    chapter_list.add(chapter);
                } else {
                    chapter_list.add(chapter + "_" + temp_index);
                }
                if (temp.length() > temp_length){
                    temp_content_list.add(temp.substring(0,temp_length));
                    temp = temp.substring(temp_length);
                } else {
                    temp_content_list.add(temp);
                    break;
                }
                temp_index++;
            }
            i++;
        }

        //2.创建目录
        File file = new File(DiskCache.path + File.separator + "book" + File.separator + book_id);
        if (!file.exists()) {
            if (!file.mkdir()) return false;
        }
        String file_dir = file.getPath();

        //章节目录
        LinkedHashMap<String,Object> chapter = new LinkedHashMap<>();
        //循环生成章节TXT文件
        for (i = 0; i < temp_content_list.size(); i++) {
            //2.在目录下创建TXT文件
            try (FileWriter fr = new FileWriter(file_dir + File.separator + "book_chapter" + File.separator + i)){
                fr.write(temp_content_list.get(i));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            chapter.put(chapter_list.get(i),"/" + i);
        }

        try {
            FileWriter f = new FileWriter(file_dir + File.separator + "chapter");
            f.write(JSONObject.toJSONString(chapter));
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
