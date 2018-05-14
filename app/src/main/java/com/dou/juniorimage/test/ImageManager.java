package com.dou.juniorimage.test;

import java.util.ArrayList;
import java.util.List;

public class ImageManager {
    private static final String TAG = "ImageManager";


    private static ImageManager imageManager = new ImageManager();

    private ImageManager(){

    }
    public static ImageManager getInstance() {
        return imageManager;
    }

    private List<String> imgNameList = new ArrayList<>();

    public void initBitmapFileNames(List<String> imgNameList) {
        this.imgNameList.clear();
        this.imgNameList.addAll(imgNameList);
    }

    private int compressWidth;
    private int compressHeight;
    public void setCompressSize(int width, int height){
        this.compressWidth = width;
        this.compressHeight = height;
    }

    public int getCompressWidth() {
        return compressWidth;
    }
    public int getCompressHeight(){
        return compressHeight;
    }

    public int getCount() {
        return imgNameList.size();
    }

    public String getImageUrl(int i) {
        return imgNameList.get(i);
    }


    public List<String> getCompessImages() {
        return imgNameList;
    }
}
