package com.dou.juniorimage.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;


import com.dou.juniorimage.LoadUtil;
import com.dou.juniorimage.MyUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LocalCache {
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;
    private static final String TAG = "LocalCache";

    private Context mContext;
    private DiskLruCache mDiskLruCache ;
    private boolean mIsDiskLruCacheCreated = false;

    public LocalCache(Context context) {
        this.mContext = context;
        File diskCacheDir = getDiskCacheDir(mContext,"bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        try {
            if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, getAppVersion(mContext), 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
    private File getDiskCacheDir(Context context, String bitmap) {
        File cacheDir = new File(context.getCacheDir().getPath()+"/"+bitmap);
        return cacheDir;
    }

    public Bitmap get(String url, int width, int height){

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "Load bitmap in UI Thread!!!");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        String key = MyUtils.hashKeyFromUrl(url);
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                FileDescriptor fileDescriptor = fileInputStream.getFD();
                bitmap = LoadUtil.decodeFromFileDescriptor(fileDescriptor,width,height);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public void put(String url, Bitmap bitmap) {
        if (mDiskLruCache == null || bitmap == null) {
            return ;
        }
        String key = MyUtils.hashKeyFromUrl(url);
        DiskLruCache.Editor editor = null;
        BufferedOutputStream bufferedOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(LoadUtil.toArray(bitmap)));
                bufferedOutputStream = new BufferedOutputStream(outputStream, LoadUtil.IO_BUFFER_SIZE);
                int b;
                while ((b = bufferedInputStream.read()) != -1){
                    bufferedOutputStream.write(b);
                }
                editor.commit();
                Log.d(TAG,"put bitmap: success");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (editor != null) {
                try {
                    editor.abort();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }finally {
            MyUtils.close(bufferedInputStream);
            MyUtils.close(bufferedOutputStream);
        }
    }

    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }



    public boolean isDiskLruCacheCreated() {
        return mIsDiskLruCacheCreated;
    }
}
