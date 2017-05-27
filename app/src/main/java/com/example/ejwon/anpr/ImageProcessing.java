package com.example.ejwon.anpr;

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
    private static MatOfRect plates;

    public static void detectNumberPlate(Mat mGray, CascadeClassifier mJavaDetector) {

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
