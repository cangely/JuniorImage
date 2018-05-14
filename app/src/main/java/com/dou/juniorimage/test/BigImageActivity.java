package com.dou.juniorimage.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.dou.juniorimage.R;

import java.util.LinkedList;
import java.util.List;

public class BigImageActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,ImageFragment.OnFragmentInteractionListener {

    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_image);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        initViews();
    }
    private void initViews(){

        Intent intent = getIntent();
        int index = intent.getIntExtra("showIndex",0);


        int count = ImageManager.getInstance().getCount();
        List<ImageFragment> fragmentList = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            String imageUrl = ImageManager.getInstance().getImageUrl(i);
            ImageFragment imageFragment = ImageFragment.newInstance(imageUrl);
            fragmentList.add(imageFragment);
        }

        viewPager = findViewById(R.id.viewpagerBigImage);
        viewPager.setOffscreenPageLimit(3);
        Log.i("BigImageActivity","index："+index);

        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),fragmentList));
        viewPager.setCurrentItem(index);

        viewPager.setOnPageChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
