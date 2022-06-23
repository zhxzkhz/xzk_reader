package com.zhhz.reader.rule;

import static org.junit.Assert.*;

import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.DiskCache;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class JsoupAnalysisTest {

    static JsoupAnalysis analysis;

    private String detail_url;

    @BeforeClass
    public static void run() throws IOException, ClassNotFoundException {
        DiskCache.path="D:\\星-阅读";
        analysis = new JsoupAnalysis("D:\\星-阅读\\rule\\www_biquxs_la.json");
    }

    @Test
    public void bookSearch(){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        analysis.BookSearch("魔王", (data, msg, label) -> {
            countDownLatch.countDown();
            assertTrue(((List<SearchResultBean>) data).size() > 0);
            detail_url = ((List<SearchResultBean>) data).get(0).getUrl();
            System.out.println("\033[0;32mBookSearch -> 通过 : " + detail_url);
        },1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void bookDetail() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        analysis.BookDetail("https://www.biquxs.la/27/27075/", (data, msg, label) -> {
            assertNotNull(((BookBean) data).getTitle());
            System.out.println("\033[0;32mBookDetail -> 通过 -> " + ((BookBean) data).getCatalogue());
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void bookDirectory() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        analysis.BookDirectory("https://www.biquxs.la/27/27075/", (data, msg, label) -> {
            assertTrue(((Map<String,String>) data).size() > 0);
            System.out.println("\033[0;32mbookSearch -> 通过 -> " + data);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void bookChapters() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(UUID.randomUUID().toString());
        bookBean.setCatalogue("https://www.cyewx.com/27/27075/11713970.html");
        analysis.BookChapters(bookBean, (data, msg, label) -> {
            assertNotNull(data);
            System.out.println("\033[0;32mbookSearch -> 通过 -> " + data);
            countDownLatch.countDown();
        },1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}