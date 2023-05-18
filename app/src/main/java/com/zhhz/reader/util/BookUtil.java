package com.zhhz.reader.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zhhz.reader.MyApplication;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.rule.RuleAnalysis;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

    public static void imageToZip(BookBean bean,boolean all,ImageToZip$Callback callback) {
        futures.add(CompletableFuture.runAsync(() -> {
            LinkedHashMap<String, String> chapter;
            try {
                Type link = TypeReference.intern(new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, LinkedHashMap.class));
                chapter = JSONObject.parseObject(Files.newInputStream(Paths.get(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "chapter")), link);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


            ZipFile zipFile = new ZipFile(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "aaaa.zip");
            zipFile.setRunInThread(true);

            //添加一个标记，用于防止死循环
            boolean bool = true;
            AtomicInteger count = new AtomicInteger(0);
            RuleAnalysis rule;
            try {
                rule = new RuleAnalysis(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "rule",false);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            //章节顺序
            AtomicInteger i = new AtomicInteger(1);

            for (Map.Entry<String, String> entry : chapter.entrySet()) {
                //图片顺序
                AtomicInteger index = new AtomicInteger(1);
                File file =new File(DiskCache.path + File.separator + "book" + File.separator + bean.getBook_id() + File.separator + "book_chapter" + File.separator + StringUtil.getMD5(entry.getValue()));
                if (!(file.isFile() || all)) continue;
                rule.bookChapters(bean, entry.getValue(), (data, tag) -> {
                    String[] c = data.getData().split("\n");
                    for (String s : c) {
                        String path1 = GlideGetPath.getCacheFileKey(s);
                        assert path1 != null;
                        if (new File(path1).isFile()) {
                            try {
                                ZipParameters zipParameters = new ZipParameters();
                                zipParameters.setFileNameInZip(i.get() + "-" + entry.getKey() + "/" + index.getAndIncrement() + ".png");
                                InputStream is = Files.newInputStream(Paths.get(path1));
                                zipFile.addStream(is, zipParameters);
                                is.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (all){
                            count.addAndGet(1);
                            ZipParameters zipParameters = new ZipParameters();
                            zipParameters.setFileNameInZip(i.get() + "-" + entry.getKey() + "/" + index.getAndIncrement() + ".png");
                            GlideApp.with(MyApplication.context).asBitmap().load(s).into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                    try {
                                        ByteArrayInputStream b = new ByteArrayInputStream(outputStream.toByteArray());
                                        zipFile.addStream(b, zipParameters);
                                        outputStream.close();
                                        resource.recycle();
                                        b.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        count.addAndGet(-1);
                                    }
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    count.addAndGet(-1);
                                }
                            });
                        }
                    }
                },entry.getKey());
                i.addAndGet(1);
            }

            while (count.get() != 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            callback.accept(true);
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, XluaTask.getThreadPool()));

    }

    public interface ImageToZip$Callback {
        void accept(boolean status);
    }
    public interface GetCacheSize$Callback {
        void accept(AtomicLong atomicLong, CopyOnWriteArrayList<String> fileList, CopyOnWriteArrayList<String> deficiencyList);
    }

}
