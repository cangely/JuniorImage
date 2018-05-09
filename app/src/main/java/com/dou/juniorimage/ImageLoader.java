package com.dou.juniorimage;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageLoader {
    private ImageLoader() {
        imageCache = new MemCache();
    }


    private static class ImageLoaderHolder {
        private static ImageLoader imageLoader = new ImageLoader();

    }

    public static ImageLoader getInstance() {
        return ImageLoaderHolder.imageLoader;
    }

    private ImageCache imageCache;
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 30, 30,TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2),
                new ThreadPoolExecutor.CallerRunsPolicy());

    public void init() {

    }

    public void setImageCache(ImageCache imageCache) {
        this.imageCache = imageCache;
    }

    public void display(String imageUrl, ImageView imageView) {
        Bitmap bitmap = imageCache.get(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        requestLoad(imageUrl,imageView,false);
    }
    public void display(String imageUrl, ImageView imageView,boolean isThumbNail) {
        Bitmap bitmap = imageCache.get(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        requestLoad(imageUrl,imageView,isThumbNail);
    }

    private void requestLoad(final String imageUrl, final ImageView imageView, final boolean isThumbNail) {
        imageView.setTag(imageUrl);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap;
                if (isThumbNail) {
                    bitmap = loadThumbNail(imageUrl);
                } else {
                    bitmap = loadImage(imageUrl);
                }

                if (bitmap == null) {
                    return;
                }
                if (imageView.getTag().equals(imageUrl)) {
                    imageView.setImageBitmap(bitmap);
                    imageCache.put(imageUrl,bitmap);
                }
            }
        });
    }

    private Bitmap loadImage(String imageUrl) {
        if (imageUrl.startsWith("http")) {
            return LoadUtil.loadFromHttp(imageUrl);
        } else {
            return LoadUtil.loadFromFile(imageUrl);
        }
    }
    private Bitmap loadThumbNail(String imageUrl) {
        if (imageUrl.startsWith("http")) {
            return LoadUtil.loadFromHttp(imageUrl);
        } else {
            return LoadUtil.loadThumbNail(imageUrl);
        }
    }
}
