package com.zhhz.reader.util;


import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;


import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;



@GlideModule
public class XluaGlideModule extends AppGlideModule {

    public static String[] getDns() {
        return Dns;
    }

    public static void setDns(String[] dns) {
        Dns = dns;
    }

    //Dns 用于okhttp3的dns
    private static String[] Dns = null;

    @Override
    public boolean isManifestParsingEnabled() {
            return false;
        }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        //储存上限10G图片
        builder.setDiskCache(new DiskLruCacheFactory("/storage/emulated/0/星空/image_discCache/", 10L * 1024 * 1024 * 1024));
        super.applyOptions(context, builder);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient.Builder builder =new OkHttpClient.Builder();
        ArrayList<Protocol> al=new ArrayList<>();
        al.add(Protocol.HTTP_1_1);
        al.add(Protocol.HTTP_2);
        builder.protocols(al);

        if  (Dns != null && Dns.length>0) {
            builder.dns(str -> {
                try {
                    ArrayList<InetAddress> arrayList = new ArrayList<>();
                    if (XluaGlideModule.Dns != null && XluaGlideModule.Dns.length > 0) {
                        for (String byName : XluaGlideModule.Dns) {
                            arrayList.add(InetAddress.getByName(byName));
                        }
                    }
                    return arrayList;
                } catch (Exception e) {
                    e.printStackTrace();
                    return okhttp3.Dns.SYSTEM.lookup(str);
                }
            });
        }


/*
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 拦截请求，获取到该次请求的request
                Request request = chain.request();
                Request.Builder r = request.newBuilder().header("user-agent", "okhttp/3.8.1");
                return chain.proceed(r.build());
            }
        });*/

        //添加拦截器（ProgressInterceptor 用于进度获取）
        //builder.addInterceptor(new ProgressInterceptor());

        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(builder.build()));
        super.registerComponents(context, glide, registry);
    }
}
