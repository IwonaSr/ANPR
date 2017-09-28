package com.example.ejwon.anpr;

import android.util.Log;

import com.hazuu.uitanpr.neural.KohonenNetwork;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Created by Ejwon on 2017-05-27.
 */
public final class ImageProcessing {

    private static float mRelativePlateSize = 0.2f; // 20%
    private static int mAbsolutePlateSize = 0;
    // This variable is used to to store the d;etected plates in the result
    private static MatOfRect plates;
    private static int number = 0;
    private static final String TAG = "ImageProcessing.java";


//    public static void detectNumberPlate(Mat mGray, CascadeClassifier mJavaDetector, PlateView plateView, ImageView imageView) { //jesli zdjecie
    public static void detectNumberPlate(Mat mGray, CascadeClassifier mJavaDetector, PlateView plateView, KohonenNetwork net) {

//        //http://opencv-java-tutorials.readthedocs.io/en/latest/06-face-detection-and-tracking.html - podobny przyklad parametrów detectmultiscale
        if (mAbsolutePlateSize == 0) {
            int heightGray = mGray.rows(); //stala wartosc kadru
            if (Math.round(heightGray * mRelativePlateSize) > 0) {
                mAbsolutePlateSize = Math.round(heightGray
                        * mRelativePlateSize); // minimalny rozmiar tablicy musi wynosić coajmniej 20% wysokosci zdjęcia czyli musimy przyblizyć kadr do takiej odleglosci aby
                //tablica stanwoila minimalny rozmiar 20% wyokosci zdjecia/kadru czyl mniej więcej 216
                Log.d(TAG, "mAbsolutePlateSize: " + mAbsolutePlateSize);

            }
        }
    //http://docs.opencv.org/2.4/modules/objdetect/doc/cascade_classification.html
        plates = new MatOfRect();

            if (mJavaDetector != null) {
                //zmieniona sygnatura
                mJavaDetector.detectMultiScale(
                        mGray, //macierz obrazu w ktory zawiera rozpoznane obiekty (przypuszczalne tablice)
                        plates, // wektor prostokątów zawierających rozpoznany obiekt
                        1.1, //scaleFactor - parameter informujacy jak bardzo rozmiar obrazu powinien byc zredykowany do skali obrazu
                        2, //parametr ktory oznacz ile sasiednich prostokątow jest kandydatami (brane pod uwage)
                        2, //
                        new Size(mAbsolutePlateSize, mAbsolutePlateSize), //mininalny rozmiar obiektu, mniejsze obiekty są ingorowane, ustawilismy minimum 20% rozmiaru wysokosci ramki
                        new Size() //maksmalny rozmiar tablicy, wieksze sa ignorowane, niezdefiniowany
                );
//                Log.d(TAG, "mJavaDetector: " + mJavaDetector);
            }


        plateView.setPlate(plates);
        plateView.setGrayMat(mGray);
        plateView.setNetwork(net);
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
