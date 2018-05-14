package com.dou.juniorimage;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

public class LoadUtil {
    private static final String TAG = "LoadUtil";

    public static Bitmap loadFromFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
    public static Bitmap loadFromFile(String fileName, int width, int height) {
        Log.d(TAG, "loadFromFile:" + width + " * " + height);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName,options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName,options);
    }

    public static final int IO_BUFFER_SIZE = 8 * 1024;

    public static Bitmap loadFromHttp(String imageUrl) {
        if (imageUrl.startsWith("https:")) {
            return loadFromHttps(imageUrl);
        }
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            bufferedInputStream = new BufferedInputStream(connection.getInputStream(),IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream);
        } catch (Exception e) {
            Log.e(TAG, "load bitmap failed:" + imageUrl);
            e.printStackTrace();
        } finally {
            MyUtils.close(bufferedInputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return bitmap;
    }
    public static Bitmap loadFromHttps(String imageUrl) {
        SSLContext sslContext = sslContextForTrustedCertificates();
        HttpsURLConnection connection = null;
        Bitmap bitmap = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier(DO_NOT_VERIFY);
            bufferedInputStream = new BufferedInputStream(connection.getInputStream(),IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream);
        } catch (Exception e) {
            Log.e(TAG, "load bitmap failed:" + imageUrl);
            e.printStackTrace();
        } finally {
            MyUtils.close(bufferedInputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return bitmap;
    }
    public static Bitmap loadFromResource(Resources res, int resId, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }



    public static Bitmap decodeFromFileDescriptor(FileDescriptor fileDescriptor, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        options.inJustDecodeBounds = true;
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor);
    }
    private static int calculateInSampleSize(BitmapFactory.Options options, int width, int height) {
        if (width == 0 || height == 0) {
            return 1;
        }
        final int rawWidth = options.outWidth;
        final int rawHeight = options.outHeight;
        Log.d(TAG, "origin size: " + rawWidth + " * " + rawHeight);
        int inSampleSize = 1;

        if (rawWidth > width || rawHeight > height) {
            final int halfWidth = rawWidth / 2;
            final int halfHeight = rawHeight / 2;
            while (halfWidth / inSampleSize >= width
                    && halfHeight / inSampleSize >= height) {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, "inSampleSize:" + inSampleSize);
        return inSampleSize;
    }

    public static boolean loadFromHttpToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection httpURLConnection = null;
        BufferedOutputStream bufferedOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            final URL url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream(),IO_BUFFER_SIZE);
            bufferedOutputStream = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = bufferedInputStream.read()) != -1){
                bufferedOutputStream.write(b);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "DownloadBitmap failed: "+e);
        }finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            MyUtils.close(bufferedOutputStream);
            MyUtils.close(bufferedInputStream);
        }
        return false;
    }
    public static byte[] toArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static SSLContext sslContextForTrustedCertificates() {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            //javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (KeyManagementException e) {
            e.printStackTrace();
        }finally {
            return sc;
        }
    }
    private static class miTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
    static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
