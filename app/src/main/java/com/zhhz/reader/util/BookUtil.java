package com.zhhz.reader.util;

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
import org.apache.commons.compress.archivers.zip.StreamCompressor;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;

import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.compress.utils.BoundedInputStream;

public class BookUtil {

    private static final List<CompletableFuture<?>> futures = new ArrayList<>();

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

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
                                try(FileReader fileReader = new FileReader(listFile)) {
                                    try(BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                                        bufferedReader.lines().forEach(copyOnWriteArrayList::add);
                                    }
                                }
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


    /**
     * 一个用于张图片导出到zip文件方法
     * @param bean {@link com.zhhz.reader.bean.BookBean } 对象
     * @param all 为 true 时导出所有章节图片，包含未完成的
     * @param callback {@link ImageToZip$Callback} 回调方法
     */
    public static void imageToZip(BookBean bean, boolean all, ImageToZip$Callback callback) {
        callback.accept(false, "开始打包", 0, 1);
        futures.add(CompletableFuture.runAsync(() -> {
            LinkedHashMap<String, String> chapter;
            try {
                Type link = TypeReference.intern(new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, LinkedHashMap.class));
                try (final InputStream is = Files.newInputStream(Paths.get(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "chapter"))){
                    chapter = JSONObject.parseObject(is, link);
                }
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
                            header.setHeader(entry1.getKey(),String.valueOf(entry1.getValue()));
                        }
                        header_x.clear();
                    }
                    if (rule.getAnalysis().getJson().getImgHeader().getReuse()) {
                        JSONObject header_x = JSONObject.parseObject(rule.getAnalysis().getJson().getHeader());
                        for (Map.Entry<String, Object> entry1 : header_x.entrySet()) {
                            header.setHeader(entry1.getKey(), String.valueOf(entry1.getValue()));
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

            callback.accept(false, "读取目录中（" + i.get() + "/" + chapter.size() + "）", 0, chapter.size());
            for (Map.Entry<String, String> entry : chapter.entrySet()) {
                File file = new File(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "book_chapter" + File.separator + StringUtil.getMD5(entry.getValue()));
                if (!(file.isFile() || all)) {
                    i.getAndIncrement();
                    callback.accept(false, "读取目录中（" + i.get() + "/" + chapter.size() + "）", i.get(), chapter.size());
                    chapterList.add(null);
                    imageList.add(null);
                    continue;
                }
                try {
                    sign.addAndGet(1);
                    rule.bookChapters(bean, entry.getValue(), (data, tag) -> {
                        i.getAndIncrement();
                        callback.accept(false, "读取目录中（" + i.get() + "/" + chapter.size() + "）", i.get(), chapter.size());
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

            }


            try {
                ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(() -> {
                    if (sign.get() == 0) {
                        throw new CancellationException();
                    }
                }, 0, 1, TimeUnit.SECONDS);
                future.get();
            } catch (InterruptedException | ExecutionException ignored) {

            }

            callback.accept(false, "读取目录完成", 1, 1);

            AtomicInteger list_index = new AtomicInteger(0);
            i.set(0);

            callback.accept(false, "读取目录完成，开始加载图片", 1, 1);

            List<CompletableFuture<Integer>> list = new ArrayList<>();
            CopyOnWriteArrayList<ZipArchiveEntryObject> zip_list = new CopyOnWriteArrayList<>();

            DefaultBackingStoreSupplier sb = new DefaultBackingStoreSupplier(null);

            for (int x = 0; x < (Math.min(chapterList.size(), 4)); x++) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    int length;
                    while ((length = list_index.getAndIncrement()) < chapterList.size()) {
                        String chapterName = chapterList.get(length);
                        if (chapterName == null) continue;
                        ArrayList<String> chapter_temp = imageList.get(length);
                        int index = 1;
                        for (String path1 : chapter_temp) {
                            i.getAndIncrement();
                            String p = GlideGetPath.getCacheFileKey(path1);
                            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(length + "-" + chapterName + "/" + (index++) + ".png");
                            zipArchiveEntry.setMethod(ZipArchiveEntry.DEFLATED);
                            zipArchiveEntry.setUnixMode(UnixStat.FILE_FLAG | 436);
                            callback.accept(false, chapterName + " - " + i.get() + "图片加载中", i.get(), count.get());
                            InputStream is = null;
                            try {
                                if (p != null && new File(p).isFile()) {
                                    is = Files.newInputStream(Paths.get(p));
                                } else if (all) {
                                    FutureTarget<Bitmap> temp = GlideApp.with(MyApplication.context).asBitmap().load(new GlideUrl(path1, headers)).submit();
                                    Bitmap resource = temp.get();
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                    is = new ByteArrayInputStream(outputStream.toByteArray());
                                    resource.recycle();
                                    outputStream.close();
                                }
                                final ScatterGatherBackingStore bs = sb.get();
                                final StreamCompressor sc = StreamCompressor.create(Deflater.DEFAULT_COMPRESSION, bs);
                                sc.deflate(is, ZipArchiveEntry.DEFLATED);
                                zipArchiveEntry.setSize(sc.getBytesRead());
                                zipArchiveEntry.setCrc(sc.getCrc32());
                                zipArchiveEntry.setCompressedSize(sc.getBytesWrittenForLastEntry());
                                sc.close();
                                zip_list.add(new ZipArchiveEntryObject(zipArchiveEntry, bs));
                                assert is != null;
                                is.close();
                            } catch (Exception e) {
                                callback.accept(false, chapterName + " - " + i.get() + "图片加载失败", i.get(), count.get());
                                e.printStackTrace();
                                LogUtil.error(e);
                            }
                        }
                    }
                    return null;
                }, XluaTask.getThreadPool()));
            }
            //图片加载是否已完成
            AtomicBoolean bool = new AtomicBoolean(false);
            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
                callback.accept(false, "图片加载完成", i.get(), count.get());
                list.clear();
                bool.set(true);
            });

            try {
                ZipArchiveOutputStream zipArchive = new ZipArchiveOutputStream(new File(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + bean.getTitle() + ".zip"));
                zipArchive.setEncoding("UTF-8");

                try {
                    ScheduledFuture<?> future = scheduledExecutorService.scheduleWithFixedDelay(() -> {
                        if (bool.get() && zip_list.size() == 0) {
                            throw new CancellationException();
                        }
                        while (zip_list.size() > 0) {
                            try {
                                ZipArchiveEntryObject zip = zip_list.remove(0);
                                try (final BoundedInputStream is = new BoundedInputStream(zip.sb.getInputStream(), zip.zipArchiveEntry.getCompressedSize())) {
                                    zipArchive.addRawArchiveEntry(zip.zipArchiveEntry, is);
                                    zip.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, 100, TimeUnit.MILLISECONDS);
                    future.get();
                } catch (InterruptedException | ExecutionException ignored) {}
                zipArchive.close();
                callback.accept(true, "打包成功", i.get(), count.get());
            } catch (Exception e) {
                callback.accept(true, "打包失败", 0, 1);
                e.printStackTrace();
            }

        }, XluaTask.getThreadPool()));

    }

    static class ZipArchiveEntryObject {
        final ZipArchiveEntry zipArchiveEntry;
        final ScatterGatherBackingStore sb;

        public ZipArchiveEntryObject(ZipArchiveEntry zipArchiveEntry, ScatterGatherBackingStore sb) {
            this.zipArchiveEntry = zipArchiveEntry;
            this.sb = sb;
        }

        public void close() throws IOException {
            sb.close();
        }

    }

    public interface ImageToZip$Callback {
        void accept(boolean status, String msg, int progress, int max);
    }

    public interface GetCacheSize$Callback {
        void accept(AtomicLong atomicLong, CopyOnWriteArrayList<String> fileList, CopyOnWriteArrayList<String> deficiencyList);
    }

}
