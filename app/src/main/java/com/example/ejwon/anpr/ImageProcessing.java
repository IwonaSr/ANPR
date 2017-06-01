package com.example.ejwon.anpr;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Created by Ejwon on 2017-05-27.
 */
public final class ImageProcessing {

    private static float mRelativePlateSize = 0.2f;
    private static int mAbsolutePlateSize = 0;
    // This variable is used to to store the d;etected plates in the result
    private static MatOfRect plates;
    private static int number = 0;
    private static final String TAG = "ImageProcessing.java";


//    public static void detectNumberPlate(Mat mGray, CascadeClassifier mJavaDetector, PlateView plateView, ImageView imageView) { //jesli zdjecie
    public static void detectNumberPlate(Mat mGray, CascadeClassifier mJavaDetector, PlateView plateView) {

        if (mAbsolutePlateSize == 0) {
            int heightGray = mGray.rows();
            if (Math.round(heightGray * mRelativePlateSize) > 0) {
                mAbsolutePlateSize = Math.round(heightGray
                        * mRelativePlateSize);
                Log.d(TAG, "mAbsolutePlateSize: " + mAbsolutePlateSize);

            }
        }

        plates = new MatOfRect();
            if (mJavaDetector != null) {
                mJavaDetector.detectMultiScale(
                        mGray,
                        plates,
                        1.1,
                        2,
                        2,
                        new Size(mAbsolutePlateSize, mAbsolutePlateSize),
                        new Size()
                );
                Log.d(TAG, "mJavaDetector: " + mJavaDetector);
            }


        plateView.setPlate(plates);
        //draw on camera output
        plateView.postInvalidate(); //camera

        //draw rectangle on loaded image
        //wywietlanie zdjecia -- odkomentujponizej
//        Bitmap bitmap = ImageLoader.loadImageAsBitmap();
//        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(mutableBitmap);
//        plateView.draw(canvas);
//        imageView.setImageBitmap(mutableBitmap);



    }
}
