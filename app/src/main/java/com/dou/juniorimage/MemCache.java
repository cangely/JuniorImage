package com.dou.juniorimage;

import android.graphics.Bitmap;
import android.util.LruCache;

public class MemCache implements ImageCache {
    private LruCache<String, Bitmap> mCache;

    public MemCache() {
        this.mCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8));
    }

    @Override
    public Bitmap get(String url) {
        return mCache.get(url);
    }

    @Override
    public void put(String url, Bitmap bitmap) {
        mCache.put(url, bitmap);
    }
}
