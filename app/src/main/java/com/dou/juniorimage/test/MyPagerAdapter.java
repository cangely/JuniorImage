package com.dou.juniorimage.test;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;

public class MyPagerAdapter extends FragmentPagerAdapter {
    private List<ImageFragment> fragments;

    public MyPagerAdapter(FragmentManager fm, List<ImageFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        Log.i("MyPagerAdapter","position:"+position);
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
