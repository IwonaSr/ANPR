package com.example.ejwon.anpr;

import android.content.Context;
import android.view.View;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ejwon on 2017-03-05.
 */
public class PlateView extends View {

    Rect [] platesArray;
    List<Point> currentPlatePointList = new ArrayList<Point>();
    List<Rect> currentPlates = new ArrayList<Rect>();
    private Mat mRgba;
    private Mat mGray;
    private CascadeClassifier mJavaDetector;
    MatOfRect plates;
    private float mRelativePlateSize = 0.2f;
    private int mAbsolutePlateSize = 0;

    public PlateView(Context context) {
        super(context);
    }

    protected void processImage(byte[] data, int width, int height) {

        //downsampling, converting into grayscale
        //Don't convert the image to gray scale right from start. You will loose the color information.
        mRgba = new Mat(height, width, CvType.CV_8UC4); // unsigned 8 bit ( u - pozytywne), 4 kanałowe (0-255) RGBA - RGB z alfa (alfa 0-1)
        mGray = new Mat(height, width, CvType.CV_8UC1); // 8 bit 1 kanał szare

        // wyodrebnieneii rgb na YUV w celu uzyskania 3 kanałów Y - luminacja (jasnoc-czarno białe) UV -kolorowe (chrominacji)
        Mat mYuv = new Mat(height + height / 2, width, CvType.CV_8UC1);
        mYuv.put(0, 0, data);

        Imgproc.cvtColor(mYuv, mGray, Imgproc.COLOR_YUV420sp2GRAY); //konwersja koloru
        Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV2RGB_NV21, 3);

        if (mAbsolutePlateSize == 0) {
            int heightGray = mGray.rows();
            if (Math.round(heightGray * mRelativePlateSize) > 0) {
                mAbsolutePlateSize = Math.round(heightGray
                        * mRelativePlateSize);
            }
        }

        // This variable is used to to store the detected plates in the result
        plates = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(
                    mGray,
                    plates,
                    1.1,
                    2,
                    2,
                    new Size(mAbsolutePlateSize, mAbsolutePlateSize),
                    new Size()
            );
    }


}
