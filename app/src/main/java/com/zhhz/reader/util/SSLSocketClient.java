package com.zhhz.reader.util;

import android.annotation.SuppressLint;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLSocketClient {

    //获取这个SSLSocketFactory
    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{getTrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //获取TrustManager
    @SuppressLint("CustomX509TrustManager")
    public static X509TrustManager getTrustManager() {
        return new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                //该方法在服务端使用，用来校验客户端的安全性
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)  {
                //该方法在客户端使用，用来校验服务端的安全性
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
}