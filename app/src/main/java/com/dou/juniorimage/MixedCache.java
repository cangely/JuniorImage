package com.dou.juniorimage;

import android.graphics.Bitmap;

public class MixedCache implements ImageCache {
    private MemCache memCache;
    private LocalCache localCache;

    public MixedCache(MemCache memCache, LocalCache localCache) {
        this.memCache = memCache;
        this.localCache = localCache;
    }

    @Override
    public Bitmap get(String url) {
        Bitmap bitmap = memCache.get(url);
        if (bitmap == null) {
            bitmap = localCache.get(url);
        }
        return bitmap;
    }

    @Override
    public void put(String url, Bitmap bitmap) {
        memCache.put(url, bitmap);
        localCache.put(url, bitmap);
    }
}
