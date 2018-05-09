package com.dou.juniorimage;

import android.graphics.Bitmap;

public interface ImageCache {
    Bitmap get(String url);

    void put(String url, Bitmap bitmap);
}
