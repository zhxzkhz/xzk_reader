package com.zhhz.reader.util;

import java.security.MessageDigest;

public class StringUtil {
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
}
