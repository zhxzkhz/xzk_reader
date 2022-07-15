package com.zhhz.reader.util;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.EmptySignature;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class GlideGetPath {

    private static File f;

    public static void init() throws NoSuchFieldException, IllegalAccessException {
        if (f != null) return;
        Field field = GlideBuilder.class.getDeclaredField("diskCacheFactory");
        field.setAccessible(true);
        DiskLruCacheFactory df = (DiskLruCacheFactory) field.get(XluaGlideModule.glide);

        field = DiskLruCacheFactory.class.getDeclaredField("cacheDirectoryGetter");
        field.setAccessible(true);
        DiskLruCacheFactory.CacheDirectoryGetter dcf = (DiskLruCacheFactory.CacheDirectoryGetter) field.get(df);
        if (dcf == null) return;
        f = dcf.getCacheDirectory();

    }

    public static File getCacheFile(String url) {
        if (f == null) return null;
        DataCacheKey dataCacheKey = new DataCacheKey(new GlideUrl(url), EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(dataCacheKey);
        try {
            long cacheSize = 10L * 1024 * 1024 * 1024;
            DiskLruCache diskLruCache = DiskLruCache.open(f, 1, 1, cacheSize);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
