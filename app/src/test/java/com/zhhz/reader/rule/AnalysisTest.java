package com.zhhz.reader.rule;

import static org.junit.Assert.*;

import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.util.DiskCache;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AnalysisTest {

    @Test
    public void bookSearch() throws IOException {
        DiskCache.path="D:\\星-阅读";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        RuleAnalysis analysis = new RuleAnalysis("D:\\星-阅读\\rule\\www_biquxs_la.json");
        analysis.BookSearch("魔王", (data, msg, label) -> {

            assertTrue(((List<SearchResultBean>) data).size() > 0);
            System.out.printf("耗时 -> %d%n", System.currentTimeMillis() -time);
            System.out.println("\033[0;32mBookSearch -> 通过 : " );
            countDownLatch.countDown();
        },1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}