package com.zhhz.reader.util;

import static org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest.createZipArchiveEntryRequest;

import android.graphics.Bitmap;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.FutureTarget;
import com.zhhz.reader.MyApplication;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.rule.RuleAnalysis;

import org.apache.commons.compress.archivers.zip.DefaultBackingStoreSupplier;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;
import org.apache.commons.compress.archivers.zip.StreamCompressor;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.ScatterGatherBackingStore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;

import cn.hutool.core.util.ObjectUtil;

public class BookUtil {

    private static final List<CompletableFuture<?>> futures = new ArrayList<>();

    public static void cancel() {
        for (CompletableFuture<?> future : futures) {
            future.cancel(true);
        }
    }

    public static void GetCacheSize(BookBean bean, GetCacheSize$Callback callback) {
        GetCacheSize(Collections.singletonList(bean), callback);
    }

    public static void GetCacheSize(List<BookBean> beans, GetCacheSize$Callback callback) {
        futures.add(CompletableFuture.runAsync(() -> {
            long count_size = 0;
            ArrayList<File> paths = new ArrayList<>();

            for (BookBean bean : beans) {
                if (bean == null) break;
                String path = DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id();
                count_size += (long) FileSizeUtil.getFileOrFilesSize(path, FileSizeUtil.SIZE_TYPE_B);
                //判断是否是漫画
                if (bean.getBook_id().equals(StringUtil.getMD5(bean.getTitle() + "▶☀true☀◀" + bean.getAuthor()))) {
                    File file = new File(path + File.separator + "book_chapter");
                    if (file.isDirectory()) {
                        paths.add(file);
                    }
                }
            }

            long finalCount_size = count_size;
            _GetCacheSize(paths, (atomicLong, fileList, deficiencyList) -> {
                atomicLong.addAndGet(finalCount_size);
                callback.accept(atomicLong, fileList, deficiencyList);
            });

        }, XluaTask.getThreadPool()));
    }


    private static void _GetCacheSize(List<File> file_list, GetCacheSize$Callback callback) {
        CompletableFuture.runAsync(() -> {
            AtomicLong count_size = new AtomicLong();
            AtomicInteger list_index = new AtomicInteger(0);
            AtomicInteger deficiency = new AtomicInteger(0);
            List<CompletableFuture<Integer>> list = new ArrayList<>();
            //图片地址列表
            CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
            //文件列表
            CopyOnWriteArrayList<String> fileList = new CopyOnWriteArrayList<>();
            //缺失文件列表
            CopyOnWriteArrayList<String> deficiencyList = new CopyOnWriteArrayList<>();

            ArrayList<File> paths = new ArrayList<>(file_list);

            for (int x = 0; x < (Math.min(paths.size(), 4)); x++) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    int length;
                    while ((length = list_index.getAndIncrement()) < paths.size()) {
                        File path_temp = paths.get(length);
                        for (File listFile : Objects.requireNonNull(path_temp.listFiles())) {
                            try {
                                FileReader fileReader = new FileReader(listFile);
                                BufferedReader bufferedReader = new BufferedReader(fileReader);
                                bufferedReader.lines().forEach(copyOnWriteArrayList::add);
                                bufferedReader.close();
                                fileReader.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    return null;
                }, XluaTask.getThreadPool()));
            }

            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
            list.clear();
            list_index.set(0);

            for (int x = 0; x < (Math.min(copyOnWriteArrayList.size(), 4)); x++) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    int length;
                    while ((length = list_index.getAndIncrement()) < copyOnWriteArrayList.size()) {
                        String path1 = GlideGetPath.getCacheFileKey(copyOnWriteArrayList.get(length));
                        long len = (long) FileSizeUtil.getFileOrFilesSize(path1, FileSizeUtil.SIZE_TYPE_B);
                        count_size.addAndGet(len);
                        if (len > 0) {
                            fileList.add(path1);
                        } else {
                            deficiencyList.add(copyOnWriteArrayList.get(length));
                            deficiency.addAndGet(1);
                        }
                    }
                    return null;
                }, XluaTask.getThreadPool()));
            }

            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).thenRunAsync(() -> callback.accept(count_size, fileList, deficiencyList));

        }, XluaTask.getThreadPool());
    }

    public static void imageToZip(BookBean bean, boolean all, ImageToZip$Callback callback) {
        callback.accept(false,"开始打包",0,1);
        futures.add(CompletableFuture.runAsync(() -> {
            LinkedHashMap<String, String> chapter;
            try {
                Type link = TypeReference.intern(new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, LinkedHashMap.class));
                chapter = JSONObject.parseObject(Files.newInputStream(Paths.get(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "chapter")), link);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            //用于判断是否完成
            AtomicInteger sign = new AtomicInteger(0);
            RuleAnalysis rule;
            LazyHeaders headers;
            try {
                rule = new RuleAnalysis(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "rule", false);
                LazyHeaders.Builder header = new LazyHeaders.Builder();
                if (rule.getAnalysis().getJson().getImgHeader() != null) {
                    if (ObjectUtil.isNotEmpty(rule.getAnalysis().getJson().getImgHeader().getHeader())) {
                        JSONObject header_x = JSONObject.parseObject(rule.getAnalysis().getJson().getImgHeader().getHeader());
                        for (Map.Entry<String, Object> entry1 : header_x.entrySet()) {
                            header.addHeader(entry1.getKey(), (String) entry1.getValue());
                        }
                        header_x.clear();
                    }
                    if (rule.getAnalysis().getJson().getImgHeader().getReuse()) {
                        JSONObject header_x = JSONObject.parseObject(rule.getAnalysis().getJson().getHeader());
                        for (Map.Entry<String, Object> entry1 : header_x.entrySet()) {
                            header.addHeader(entry1.getKey(), (String) entry1.getValue());
                        }
                        header_x.clear();
                    }
                }
                headers = header.build();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            //章节顺序
            AtomicInteger i = new AtomicInteger(0);
            //章节名字
            ArrayList<String> chapterList = new ArrayList<>();
            //图片地址
            ArrayList<ArrayList<String>> imageList = new ArrayList<>();

            AtomicInteger count = new AtomicInteger();

            callback.accept(false,"读取目录中（" + i.get() + "/" + chapter.size() +"）",0,chapter.size());
            for (Map.Entry<String, String> entry : chapter.entrySet()) {
                File file = new File(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "book_chapter" + File.separator + StringUtil.getMD5(entry.getValue()));
                if (!(file.isFile() || all)) {
                    i.getAndIncrement();
                    callback.accept(false,"读取目录中（" + i.get() + "/" + chapter.size() +"）",i.get(),chapter.size());
                    chapterList.add(null);
                    imageList.add(null);
                    continue;
                }
                try {
                    sign.addAndGet(1);
                    rule.bookChapters(bean, entry.getValue(), (data, tag) -> {
                        i.getAndIncrement();
                        callback.accept(false,"读取目录中（" + i.get() + "/" + chapter.size() +"）",i.get(),chapter.size());
                        ArrayList<String> imageList_c = new ArrayList<>();
                        String[] c = data.getData().split("\n");
                        for (String s : c) {
                            if (s.startsWith("http")) {
                                imageList_c.add(s);
                                count.getAndIncrement();
                            }
                        }
                        chapterList.add(entry.getKey());
                        imageList.add(imageList_c);
                        sign.addAndGet(-1);
                    }, entry.getKey());
                } catch (Exception e) {
                    sign.addAndGet(-1);
                    e.printStackTrace();
                }
                i.getAndIncrement();
            }

            while (sign.get() != 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    callback.accept(false,"目录读取失败",0,1);
                    e.printStackTrace();
                    return;
                }
            }

            AtomicInteger list_index = new AtomicInteger(0);
            i.set(0);

            callback.accept(false,"读取目录完成，开始加载图片",1,1);

            DefaultBackingStoreSupplier sb = new DefaultBackingStoreSupplier(null);
            try {
                ZipArchiveOutputStream zipArchive = new ZipArchiveOutputStream(new File(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "aaaa.zip"));
                zipArchive.setEncoding("UTF-8");

                int length;
                while ((length = list_index.getAndIncrement()) < chapterList.size()) {
                    String chapterName = chapterList.get(length);
                    if (chapterName == null) continue;
                    ArrayList<String> chapter_temp = imageList.get(length);
                    int index = 1;
                    for (String path1 : chapter_temp) {
                        String p = GlideGetPath.getCacheFileKey(path1);
                        if (p != null && new File(p).isFile()) {
                            try {
                                i.getAndIncrement();
                                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry( length + "-" + chapterName + "/" + (index++) + ".png");
                                zipArchiveEntry.setMethod(ZipArchiveEntry.DEFLATED);
                                InputStream is = Files.newInputStream(Paths.get(p));
                                zipArchiveEntry.setSize(is.available());
                                zipArchiveEntry.setUnixMode(UnixStat.FILE_FLAG | 436);
                                final ZipArchiveEntryRequest zipArchiveEntryRequest = createZipArchiveEntryRequest(zipArchiveEntry,  () -> is);
                                final ScatterGatherBackingStore bs = sb.get();
                                final StreamCompressor sc = StreamCompressor.create(Deflater.DEFAULT_COMPRESSION, bs);
                                ScatterZipOutputStream scatterZipOutputStream = new ScatterZipOutputStream(bs, sc);
                                scatterZipOutputStream.addArchiveEntry(zipArchiveEntryRequest);
                                scatterZipOutputStream.writeTo(zipArchive);
                                is.close();
                                scatterZipOutputStream.close();
                                callback.accept(false,chapterName + " - " + i.get() + "图片加载中",i.get(),count.get());
                            } catch (Exception e) {
                                callback.accept(false,chapterName + " - " + i.get() + "图片加载失败",i.get(),count.get());
                                e.printStackTrace();
                                LogUtil.error(e);
                            }
                        } else if (all) {
                            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry( length + "-" + chapterName + "/" + (index++) + ".png");
                            zipArchiveEntry.setMethod(ZipArchiveEntry.DEFLATED);
                            zipArchiveEntry.setUnixMode(UnixStat.FILE_FLAG | 436);

                            try {
                                i.getAndIncrement();
                                FutureTarget<Bitmap> temp = GlideApp.with(MyApplication.context).asBitmap().load(new GlideUrl(path1, headers)).submit();
                                Bitmap resource = temp.get();
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                ByteArrayInputStream b = new ByteArrayInputStream(outputStream.toByteArray());
                                zipArchiveEntry.setSize(b.available());
                                final ZipArchiveEntryRequest zipArchiveEntryRequest = createZipArchiveEntryRequest(zipArchiveEntry,  () -> b);
                                final ScatterGatherBackingStore bs = sb.get();
                                final StreamCompressor sc = StreamCompressor.create(Deflater.DEFAULT_COMPRESSION, bs);
                                ScatterZipOutputStream scatterZipOutputStream = new ScatterZipOutputStream(bs, sc);
                                scatterZipOutputStream.addArchiveEntry(zipArchiveEntryRequest);
                                scatterZipOutputStream.zipEntryWriter().writeNextZipEntry(zipArchive);
                                resource.recycle();
                                outputStream.close();
                                b.close();
                                scatterZipOutputStream.close();
                                callback.accept(false,chapterName + " - " + i.get() + "图片加载中",i.get(),count.get());
                            } catch (Exception e) {
                                callback.accept(false,chapterName + " - " + i.get() + "图片加载失败",i.get(),count.get());
                                e.printStackTrace();
                                LogUtil.error(e);
                            }
                        } else  {
                            i.getAndIncrement();
                        }
                    }

                }
                zipArchive.close();
                callback.accept(true,"打包成功",i.get(),count.get());
            } catch (Exception e) {
                callback.accept(true,"打包失败",0,1);
                e.printStackTrace();
            }

        }, XluaTask.getThreadPool()));

    }

    public interface ImageToZip$Callback {
        void accept(boolean status,String msg,int progress,int max);
    }

    public interface GetCacheSize$Callback {
        void accept(AtomicLong atomicLong, CopyOnWriteArrayList<String> fileList, CopyOnWriteArrayList<String> deficiencyList);
    }

}
