package com.zhhz.reader.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


/**
 * 自动识别是否是win系统，如果是则调用 java.util.Base64,反之调用 android.util.Base64
 *
 */

public class Auto_Base64 {
    private static Class<?> Base64;
    private static Object decoder;
    private static Object encoder;
    private static Class<?> Decoder;
    private static Class<?> Encoder;

    private static boolean isWin = false;

    static {
        String os = System.getProperty("os.name");
        assert os != null;
        try {
            if (os.toLowerCase().startsWith("win")) {
                isWin = true;
                Base64 = Class.forName("java.util.Base64");
                decoder = Base64.getDeclaredMethod("getDecoder").invoke(null);
                Decoder = Class.forName("java.util.Base64$Decoder");
                encoder = Base64.getDeclaredMethod("getEncoder").invoke(null);
                Encoder = Class.forName("java.util.Base64$Encoder");
            } else {
                Base64 = Class.forName("android.util.Base64");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    //解密
    public static byte[] decode(byte[] bytes) {
        return decode(new String(bytes));
    }

    //解密
    public static byte[] decode(String str) {
        try {
            if (isWin) {
                System.out.println(decoder);
                return (byte[]) Decoder.getMethod("decode",String.class).invoke(decoder,str);
            } else {
                return (byte[]) Base64.getDeclaredMethod("decode",String.class,int.class).invoke(null,str, 0);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    //解密
    public static String decodeToString(String str) {
        return new String(decode(str));
    }

    public static String encodeToString(byte[] str) {
        return new String(encode(str));
    }

    public static String encodeToString(String str) {
        return new String(encode(str));
    }

    public static byte[] encode(byte[] str) {
        System.out.println(Arrays.toString(str));
        return encode(new String(str));
    }

    public static byte[] encode(String str) {
        try {
            if (isWin) {
                return (byte[]) Encoder.getMethod("encode",byte[].class).invoke(encoder, str.getBytes());
            } else {
                return (byte[]) Base64.getDeclaredMethod("encode",String.class,int.class).invoke(null,str, 0);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
