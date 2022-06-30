package com.zhhz.reader.rule;

import static org.junit.Assert.*;

import com.sun.script.javascript.RhinoScriptEngine;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.DiskCache;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class AnalysisTest {

    @Test
    public void bookSearch() throws IOException {
        DiskCache.path="D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\rule\\www_sixmh7_com.json");
        analysis.BookSearch("魔王", (data, msg, label) -> {

            assertTrue(((List<SearchResultBean>) data).size() > 0);
            System.out.printf("耗时 -> %d%n", System.currentTimeMillis() -time);
            System.out.println("\033[0;32mBookSearch -> 通过 : " );
            countDownLatch.countDown();
        },"1");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void bookDetail() throws IOException {

        DiskCache.path="D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\rule\\www_sixmh7_com.json");

        analysis.BookDetail("http://www.sixmh7.com/16041/", (data, msg, label) -> {
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

        DiskCache.path="D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\rule\\www_sixmh7_com.json");
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(UUID.randomUUID().toString());
        analysis.BookDirectory("http://www.sixmh7.com/bookchapter/@post->id=16041&id2=1", (data, msg, label) -> {
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

        DiskCache.path="D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\rule\\www_sixmh7_com.json");
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(UUID.randomUUID().toString());
        analysis.BookChapters(bookBean,"http://www.sixmh7.com/16041/143876.html", (data, msg, label) -> {
            System.out.println("data = " + data);
            assertNotNull(data);
            countDownLatch.countDown();
        },1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}