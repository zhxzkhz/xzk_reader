package com.zhhz.reader.util;

import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 用于获取文件大小
 */
public class FileSizeUtil {

    public static final int SIZE_TYPE_B = 1;//获取文件大小单位为B的double值
    public static final int SIZE_TYPE_KB = 2;//获取文件大小单位为KB的double值
    public static final int SIZE_TYPE_MB = 3;//获取文件大小单位为MB的double值
    public static final int SIZE_TYPE_GB = 4;//获取文件大小单位为GB的double值
    private static final String TAG = FileSizeUtil.class.getSimpleName();

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "获取文件大小失败!");
        }
        return ConvertFileSize(blockSize, sizeType);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "获取文件大小失败!");
        }
        return ConvertFileSize(blockSize);
    }

    /**
     * 获取指定文件大小
     *
     * @param file 文件
     * @return 文件大小
     */
    private static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            size = file.length();
        } else {
            Log.e(TAG, "获取文件大小不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹
     */
    private static long getFileSizes(File f) {
        long size = 0;
        File[] file_list = f.listFiles();
        for (int i = 0; i < Objects.requireNonNull(file_list).length; i++) {
            if (file_list[i].isDirectory()) {
                size = size + getFileSizes(file_list[i]);
            } else {
                size = size + getFileSize(file_list[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param file_size 文件大小
     * @return 转换后大小
     */
    public static String ConvertFileSize(long file_size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (file_size == 0) {
            return wrongSize;
        }
        if (file_size < 1024) {
            fileSizeString = df.format((double) file_size) + "B";
        } else if (file_size < 1048576) {
            fileSizeString = df.format((double) file_size / 1024) + "KB";
        } else if (file_size < 1073741824) {
            fileSizeString = df.format((double) file_size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) file_size / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS    文件大小
     * @param sizeType 大小类型
     * @return 转换后大小
     */
    private static double ConvertFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZE_TYPE_B:
                fileSizeLong = Double.parseDouble(df.format((double) fileS));
                break;
            case SIZE_TYPE_KB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1024));
                break;
            case SIZE_TYPE_MB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1048576));
                break;
            case SIZE_TYPE_GB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }


}