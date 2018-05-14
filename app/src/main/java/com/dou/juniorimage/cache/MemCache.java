package com.dou.juniorimage.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.dou.juniorimage.MyUtils;

public class MemCache  {
    private LruCache<String, Bitmap> mCache;

    public MemCache() {
        long memMax = Runtime.getRuntime().maxMemory() / 1024 / 8;
        int memMaxInt = (int) memMax;
        Log.d("MemCache","memMax:"+memMax+" , "+memMaxInt);
        this.mCache = new LruCache<String ,Bitmap>((memMaxInt)){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    public Bitmap getBitmap(String url) {
        return mCache.get(MyUtils.hashKeyFromUrl(url));
    }

    public void putBitmap(String url, Bitmap bitmap) {
        if (bitmap != null) {
            mCache.put(MyUtils.hashKeyFromUrl(url), bitmap);
        }
    }
}
