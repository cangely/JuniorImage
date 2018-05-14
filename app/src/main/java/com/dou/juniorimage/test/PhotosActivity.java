package com.dou.juniorimage.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.dou.juniorimage.ImageLoader;
import com.dou.juniorimage.R;

import java.util.List;

public class PhotosActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "PhotosActivity";
    private GridView gridView;
    private boolean isScrollIdle = true;
    PictureAdapter pictureAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        initViews();
    }

    private void initViews() {
        gridView = findViewById(R.id.gridPhotos);
        pictureAdapter = new PictureAdapter(ImageManager.getInstance().getCompessImages(),this);
        gridView.setAdapter(pictureAdapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG,"position："+position);
                Intent intent = new Intent(PhotosActivity.this,BigImageActivity.class);
                intent.putExtra("showIndex",position % ImageManager.getInstance().getCount());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            isScrollIdle = true;
            pictureAdapter.notifyDataSetChanged();
        } else {
            isScrollIdle = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }


    public class PictureAdapter extends BaseAdapter {
        private Context context;

        private List<String> pictures;
        private int imgWidth;
        private int imgHeight;
        private ImageLoader mImageLoader;

        private Drawable mDefaultBitmapDrawable;

        public PictureAdapter(List<String> bitmaps, Context context) {
            super();
            this.context = context;
            this.pictures = bitmaps;
            this.imgWidth = ImageManager.getInstance().getCompressWidth() - 2;
//        this.imgHeight = ImageManager.getInstance().getCompressHeight();
            imgHeight = imgWidth;
            mImageLoader = ImageLoader.getInstance();
            mDefaultBitmapDrawable = context.getResources().getDrawable(R.drawable.image_default);
        }


        @Override
        public int getCount() {

            if (null != pictures) {
                return pictures.size();
            } else {
                return 0;
            }
        }

        @Override
        public String getItem(int position) {

            return pictures.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(this.context).inflate(R.layout.picture_item, null);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.ivCompessImage);
                ViewGroup.LayoutParams layoutParams = viewHolder.image.getLayoutParams();
                layoutParams.width = imgWidth;
                layoutParams.height = imgHeight;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final ImageView imageView = viewHolder.image;

            final String tag = (String)imageView.getTag();
            final String uri = getItem(position);
            if (!uri.equals(tag)) {
                imageView.setImageDrawable(mDefaultBitmapDrawable);
            }
            if (isScrollIdle) {
                mImageLoader.display(pictures.get(position), imageView, imgWidth, imgHeight);
            }
            

//        viewHolder.image.setImageBitmap(bitmap);
            return convertView;
        }

        class ViewHolder {
            public ImageView image;
        }
    }
}
