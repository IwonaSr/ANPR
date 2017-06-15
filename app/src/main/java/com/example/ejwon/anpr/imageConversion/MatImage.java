package com.example.ejwon.anpr.imageConversion;

import org.opencv.core.Mat;

/**
 * Created by Ejwon on 2017-06-04.
 */
public class MatImage {

    Mat mRgb;
    Mat mGray;

    public MatImage() {
    }

        public Mat getmRgb() {
        return mRgb;
    }

    public void setmRgb(Mat rgbMat) {
        this.mRgb = mRgb;
    }

    public MatImage(Mat mGray, Mat mRgb){
        mGray = this.mGray;
        mRgb = this.mRgb;
    }

    public Mat getmGray() {
        return mGray;
    }

    public void setmGray(Mat mGray) {
        this.mGray = mGray;
    }


}
