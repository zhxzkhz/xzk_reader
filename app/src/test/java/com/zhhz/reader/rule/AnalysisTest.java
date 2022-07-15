package com.zhhz.reader.rule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.StringUtil;

import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class AnalysisTest {

    @Test
    public void bookSearch() throws IOException {
        DiskCache.path = "D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\new\\api_255zw_com.json");
        analysis.BookSearch("魔王", (data, msg, label) -> {
            System.out.println("data.toString() = " + data.toString());
            assertTrue(((List<SearchResultBean>) data).size() > 0);
            System.out.printf("耗时 -> %d%n", System.currentTimeMillis() - time);
            System.out.println("\033[0;32mBookSearch -> 通过 : ");
            countDownLatch.countDown();
        }, "1");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void bookDetail() throws IOException {

        DiskCache.path = "D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\new\\jmcomic_asia.json");

        analysis.BookDetail("https://jmcomic.asia/album/220960/翻车汉化组-digital-lover-なかじまゆか-人妻幼馴染とひと夏のできごと3-dlo-14-中国翻訳", (data, msg, label) -> {
            System.out.println("data = " + data);
            assertNotNull(data);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void bookDirectory() throws IOException {

        DiskCache.path = "D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\new\\jmcomic_asia.json");
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(UUID.randomUUID().toString());
        String s = "https://jmcomic.asia/album/277707/%E5%BC%82%E4%B8%96%E7%95%8C%E4%B8%8D%E4%BC%A6%E5%8B%87%E8%80%85-%E7%95%B0%E4%B8%96%E7%95%8C%E4%B8%8D%E5%80%AB%E5%8B%87%E8%80%85-%E6%9E%AB%E5%8F%B6%E6%B1%89%E5%8C%96-%E3%81%84%E3%81%AE%E3%81%BE%E3%82%8B-%E5%A4%A7%E4%BA%95%E6%98%8C%E5%92%8C-%E7%95%B0%E4%B8%96%E7%95%8C%E4%B8%8D%E5%80%AB-%E9%AD%94%E7%8E%8B%E8%A8%8E%E4%BC%90%E3%81%8B%E3%82%89%E5%8D%81%E5%B9%B4-%E5%A6%BB%E3%81%A8%E3%81%AF%E3%83%AC%E3%82%B9%E3%81%AE%E5%85%83%E5%8B%87%E8%80%85%E3%81%A8-%E5%A4%AB%E3%82%92%E4%BA%A1%E3%81%8F%E3%81%97%E3%81%9F%E5%A5%B3%E6%88%A6%E5%A3%AB";
        //s="https://jmcomic.asia/album/220960/翻车汉化组-digital-lover-なかじまゆか-人妻幼馴染とひと夏のできごと3-dlo-14-中国翻訳";
        analysis.BookDirectory(s, (data, msg, label) -> {
            System.out.println("data.toString() = " + data.toString());
            assertNotNull(data);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void bookChapters() throws IOException {
        DiskCache.path = "D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\new\\jmcomic_asia.json");
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(UUID.randomUUID().toString());
        analysis.BookChapters(bookBean, "https://jmcomic.asia/photo/277709", (data, msg, label) -> {
            System.out.println("data = " + data);
            assertNotNull(data);
            countDownLatch.countDown();
        }, 1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void main() throws IOException, ClassNotFoundException {
        ObjectInputStream fis = new ObjectInputStream(Files.newInputStream(Paths.get("D:\\星-阅读\\map_xx.txt")));
        HashMap<String, String> map = (HashMap<String, String>) fis.readObject();

        HashMap<String, String> list = new HashMap<>();

        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        String key;
        String value;
        System.out.println("map = " + map);
        while (iterator.hasNext()) {
            Map.Entry<String, String> a = iterator.next();
            key = a.getKey();
            value = a.getValue();

            if (list.containsKey(value)) {
                list.put(value, list.get(value) + '|' + key.replace(".png", ""));
            } else {
                list.put(value, key.replace(".png", ""));
            }

        }

        for (Map.Entry<String, String> entry : list.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            String s = "x(/#(" + value + ")#/gi, \"" + StringUtil.cnToUnicode(key) + "\");";
            System.out.println(s);
        }

    }

}