package com.zhhz.reader.util;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.zhhz.reader.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * 文件工具类
 */

public class FileUtil {
    public static boolean CopyFile(Uri uri, String s) {
        File file = new File(s);
        if (!Objects.requireNonNull(file.getParentFile()).isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                return false;
            }
        }
        byte[] bytes = readFile(uri);
        if (bytes == null) return false;
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static boolean CopyFile(@NonNull String content, @NonNull File file) {
        if (!Objects.requireNonNull(file.getParentFile()).isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                return false;
            }
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(content.getBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static byte[] readFile(Uri uri) {
        return readFile(MyApplication.context, uri);
    }

    public static byte[] readFile(Context context, Uri uri) {
        try {
            return readFile(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readFile(String path) {
        try {
            return readFile(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static byte[] readFile(InputStream fis) {
        try {

            int size = fis.available();
            byte[] bytes = new byte[size];
            if (fis.read(bytes) != size) throw new IOException("文件读取异常");
            fis.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除文件夹
     * @param path 文件夹路径
     */
    public static void deleteFolders(String path){
        Path path1 = Paths.get(path);

        try {
            Files.walkFileTree(path1,new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LogUtil.error(e);
        }

    }

}
