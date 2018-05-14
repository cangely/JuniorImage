package com.dou.juniorimage.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.dou.juniorimage.ImageLoader;
import com.dou.juniorimage.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager wm1 = this.getWindowManager();
        int width = wm1.getDefaultDisplay().getWidth() / 3;
        int height = wm1.getDefaultDisplay().getHeight() / 3;

        ImageLoader.getInstance().init(getApplicationContext());
        ImageManager.getInstance().setCompressSize(width,height);


        initViews();
        requestPermission();


    }
    private void initViews(){
        findViewById(R.id.btnEnter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllHttpPhotoInfo();
                Intent intent = new Intent(MainActivity.this,PhotosActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnTest1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllPhotoInfo();
                Intent intent = new Intent(MainActivity.this,PhotosActivity.class);
                startActivity(intent);
            }
        });
    }

    private void requestPermission(){
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
        boolean lackPerm = ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED;
        Log.i(TAG, "lackPerm:" + lackPerm);

        if (lackPerm) {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    MY_PERMISSIONS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == MY_PERMISSIONS_REQUEST){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllPhotoInfo();
            } else {
                Toast.makeText(this, "拒绝了权限", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getAllHttpPhotoInfo();
            }
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    private void getAllHttpPhotoInfo() {
        ImageManager.getInstance().initBitmapFileNames(Arrays.asList(TestData.imageUrls));
    }
    private void getAllPhotoInfo() {
        Log.i(TAG,"getAllPhotoInfo");

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> names;
                List<String> descs;
                List<String> fileNames;

//                names = new ArrayList();
//                descs = new ArrayList();
                fileNames = new ArrayList();
                Cursor cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));

                    fileNames.add(new String(data, 0, data.length - 1));
                }
                cursor.close();
                Log.i(TAG,"fileNames size:"+fileNames.size());
                ImageManager.getInstance().initBitmapFileNames(fileNames);

            }
        }).start();
    }



    private static class MyAsyncTask extends AsyncTask<String,Integer,String>{
        private String name;

        private MyAsyncTask(String name) {
            super();
            this.name = name;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return name;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.d(TAG,result+" execute finish at "+df.format(new Date()));
        }
    }
}
