package com.dou.juniorimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.dou.juniorimage.cache.LocalCache;
import com.dou.juniorimage.cache.MemCache;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    private ImageLoader() {

    }


    private static class ImageLoaderHolder {
        private static ImageLoader imageLoader = new ImageLoader();

    }

    public static ImageLoader getInstance() {
        return ImageLoaderHolder.imageLoader;
    }

    private MemCache memCache;
    private LocalCache localCache;

    private static final int MSG_POST_RESULT = 101;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long ALIVE_KEEP = 30L;

    private static final int IO_BUFFER_SIZE = 8 * 1024;

    /**
     * 你必须首先调用这个初始化方法，框架才能正常工作.
     * @param context Application context ONLY.
     */
    @NonNull
    public ImageLoader init(Context context) {
        memCache = new MemCache();
        localCache = new LocalCache(context);
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                ALIVE_KEEP,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                mThreadFactory);
        return this;

    }

    private ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r,"ImageManager#"+mCount.getAndIncrement());
        }
    };

    private Handler mainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            String uri = (String) imageView.getTag();
            if (uri.equals(result.uri)) {
                imageView.setImageBitmap(result.bitmap);
            } else {
                Log.w(TAG, "set image, but url has changed,ignored!!!!!");
            }
        }
    };
    private ThreadPoolExecutor executor ;
    int memOnTarget = 0;
    int memOffTarget = 0;
    int diskOnTarget = 0;
    int diskOffTarget = 0;
    public Bitmap loadBitmap(String uri, int width, int height) {
        Bitmap bitmap = memCache.getBitmap(uri);
        if (bitmap != null) {
//            Log.d(TAG, "Memory cache: 命中:"+memOnTarget);
            memOnTarget++;
            return bitmap;
        } else {
            memOffTarget++;
//            Log.d(TAG, "Memory cache: 未命中:"+memOffTarget);
        }
        bitmap = localCache.get(uri, width, height);
        if (bitmap != null) {
            diskOnTarget++;
            Log.d(TAG, "Local cache: 命中:"+uri);
            return bitmap;
        }else {
            diskOffTarget++;
//            Log.d(TAG, "Local cache: 未命中:"+diskOffTarget);
        }
        if (uri.startsWith("http")) {
            bitmap = LoadUtil.loadFromHttp(uri);
//            localCache.put(uri,bitmap);
        }else {
            bitmap = LoadUtil.loadFromFile(uri,width,height);
        }
        memCache.putBitmap(uri,bitmap);

        return bitmap;
    }
    public Bitmap loadBitmap(String uri) {
        return loadBitmap(uri, 0, 0);
    }
    public void display(final String uri, final ImageView imageView) {
        display(uri, imageView, 0, 0);
    }
    public void display(final String uri, final ImageView imageView, final int width, final int height) {
        imageView.setTag(uri);
        Bitmap bitmap = memCache.getBitmap(uri);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapRunner = new Runnable() {
            @Override
            public void run() {
                Bitmap loadBmp = loadBitmap(uri, width, height);
                if (loadBmp != null) {
                    LoaderResult loaderResult = new LoaderResult(imageView, uri, loadBmp);
                    mainHandler.obtainMessage(MSG_POST_RESULT,loaderResult).sendToTarget();
                } else {
                    Log.w(TAG,"加载失败:"+uri);
                    /**
                    if (uri.startsWith("http:")){
                        String tempUrl = new String(uri);
                        tempUrl = tempUrl.replace("http","https");
                        loadBmp = loadBitmap(uri, width, height);
                        if (loadBmp != null) {
                            Log.w(TAG, "以https方式访问:" + tempUrl);
                            LoaderResult loaderResult = new LoaderResult(imageView, uri, loadBmp);
                            mainHandler.obtainMessage(MSG_POST_RESULT, loaderResult).sendToTarget();
                        } else {
                            Log.w(TAG, "以https方式访问失败:" + tempUrl);
                        }
                    }else {
                        Log.w(TAG,"加载失败:"+uri);
                    }
                     //*/
                }
            }
        };
        executor.execute(loadBitmapRunner);
    }
    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }
}
