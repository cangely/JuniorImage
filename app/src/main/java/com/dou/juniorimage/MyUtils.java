package com.dou.juniorimage;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyUtils {
    private static final String TAG = "JIMAGE";
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String hashKeyFromUrl(String url) {
        String cacheKey ;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private static  String bytesToHexString(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);//高24位置0
            if (hex.length() == 1) {
                //如果不满F，则前面补一位0
                stringBuffer.append('0');
            }
            stringBuffer.append(hex);
        }
        return stringBuffer.toString();
    }


}
