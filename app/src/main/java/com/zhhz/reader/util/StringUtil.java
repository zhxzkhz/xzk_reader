package com.zhhz.reader.util;

import cn.hutool.crypto.digest.MD5;

/**
 * 字符串工具类
 */

public class StringUtil {

    /**
     * 获取字符串MD5
     *
     * @param str 字符串
     * @return 字符串的md5
     */
    public static String getMD5(String str) {
        return MD5.create().digestHex(str);
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
