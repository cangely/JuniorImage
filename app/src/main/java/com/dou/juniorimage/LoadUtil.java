package com.dou.juniorimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoadUtil {
    public static Bitmap loadFromFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    public static Bitmap loadFromHttp(String imageUrl) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageUrl);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            bitmap = BitmapFactory.decodeStream(connection.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public static Bitmap loadThumbNail(String filePath){

        Bitmap bm = loadThumbNail(filePath, -1,-1);
        return bm;
    }
    private static Bitmap loadThumbNail(String filePath,int width ,int height){
        Log.d("loadThumbNail", "loadThumbNail");
        int h = 0;
        int w = 0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
        BitmapFactory.decodeFile(filePath, options);

        if (width > 0 && height > 0) {
            h = height;
            w = width;
        }else {
            h = options.outHeight;
            w = options.outWidth;
        }

        int inSampleSize = 2;
        int minLen = Math.min(h, w);
        if(minLen > 100) { // 如果原始图像的最小边长大于100dp
            float ratio = (float)minLen / 100.0f; // 计算像素压缩比例
            inSampleSize = (int)ratio;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        return bm;
    }
}
