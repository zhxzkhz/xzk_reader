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
        RuleAnalysis analysis = new RuleAnalysis("D:\\84dec38d427180c8dda23e7f22f7da9f\\www_diyibanzhu_xyz.json");
        analysis.BookSearch("神御", (data, msg, label) -> {
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
        RuleAnalysis analysis = new RuleAnalysis("D:\\84dec38d427180c8dda23e7f22f7da9f\\www_diyibanzhu_xyz.json");

        analysis.BookDetail("http://www.1diyibanzhu.xyz/29/29760/", (data, msg, label) -> {
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
        RuleAnalysis analysis = new RuleAnalysis("D:\\84dec38d427180c8dda23e7f22f7da9f\\xzy.json");
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(UUID.randomUUID().toString());
        analysis.BookDirectory("http://www.1diyibanzhu.xyz/29/29760_1/", (data, msg, label) -> {
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
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\rule\\www_diyibanzhu_xyz-x.json");
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(UUID.randomUUID().toString());
        analysis.BookChapters(bookBean, "http://www.1diyibanzhu.xyz/30/30283/718194_1.html", (data, msg, label) -> {
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