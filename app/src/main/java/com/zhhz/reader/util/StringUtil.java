package com.zhhz.reader.util;

import java.security.MessageDigest;

/**
 * 字符串工具类
 */

public class StringUtil {

    /**
     * 获取字符串MD5
     *
     * @param s 字符串
     * @return 字符串的md5
     */
    public static String getMD5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            StringBuilder sb = new StringBuilder();
            byte[] digestBytes = digest.digest();
            for (byte b : digestBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 中文转Unicode
     * 其他英文字母或特殊符号也可进行Unicode编码
     *
     * @param cn 中文
     * @return Unicode
     */
    public static String cnToUnicode(String cn) {
        char[] chars = cn.toCharArray();
        StringBuilder returnStr = new StringBuilder();
        for (char aChar : chars) {
            returnStr.append("\\u").append(Integer.toString(aChar, 16));
        }
        return returnStr.toString();
    }

}
