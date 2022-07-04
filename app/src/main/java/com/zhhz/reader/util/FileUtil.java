package com.zhhz.reader.util;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.zhhz.reader.sql.SQLiteUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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
        return readFile(SQLiteUtil.context, uri);
    }

    public static byte[] readFile(Context context, Uri uri) {
        try {
            InputStream fis = context.getContentResolver().openInputStream(uri);
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

}
