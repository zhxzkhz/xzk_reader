package com.zhhz.reader.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.thread.ThreadFactoryBuilder;

public class XluaTask {
    private static final ExecutorService executorService;

    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNamePrefix("Xlua-Thread-").setDaemon(true).build();
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1024);
        int corePoolSize = 5;
        int maximumPoolSize = 200;
        executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                0L, TimeUnit.MILLISECONDS,
                queue,threadFactory);
    }

    public static ExecutorService getThreadPool(){
        return executorService;
    }
}
