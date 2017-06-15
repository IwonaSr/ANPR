package com.example.ejwon.anpr.OCRRecognition;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.ejwon.anpr.ImageFormatConversion;
import com.example.ejwon.anpr.common.Utils;
import com.example.ejwon.anpr.interfaces.OnTaskCompleted;
import com.example.ejwon.anpr.models.BitmapWithCentroid;
import com.example.ejwon.anpr.neural.SampleData;
import com.hazuu.uitanpr.neural.KohonenNetwork;

import junit.framework.Assert;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OCRRecognition extends AsyncTask<Void, Bitmap, String> {

    private List<Rect> currentPlatesOnAsy;
    private Mat originImageOnAsy;
    private OnTaskCompleted listener;

    static final int DOWNSAMPLE_WIDTH = 20;
    static final int DOWNSAMPLE_HEIGHT = 50;
    private static final String TAG = "OCRRecognition.java";

    protected int pixelMap[];
    protected Bitmap newBitmap;
    protected double ratioX;
    protected double ratioY;
    protected int downSampleLeft;
    protected int downSampleRight;
    protected int downSampleTop;
    protected int downSampleBottom;
    KohonenNetwork net;
    Utils utils;
    public boolean isFail = false;
    public boolean isRunningTask = false;

    public OCRRecognition(List<Rect> currentPlates, Mat originImage, boolean isRunningTask,
                 OnTaskCompleted listener, KohonenNetwork net, Utils utils) {
        this.currentPlatesOnAsy = new ArrayList<Rect>(currentPlates);
        this.originImageOnAsy = originImage;
        this.listener = listener;
        this.isRunningTask = isRunningTask;
        this.net = net;
        this.utils = utils;
    }

    @Override
    protected void onPreExecute() {
        isRunningTask = true;
    }

    @Override
    protected String doInBackground(Void... params) {
        Iterator<Rect> iterator = currentPlatesOnAsy.iterator();
        BitmapWithCentroid tempBitmap;
        long start, timeRequired;
        String result = "";

        while (iterator.hasNext()) {
            start = System.currentTimeMillis();
            Rect plateRect = iterator.next();
            Mat plateImage;
            List<BitmapWithCentroid> charList = new ArrayList<BitmapWithCentroid>();

            int x = plateRect.x, y = plateRect.y, w = plateRect.width, h = plateRect.height;

            Rect roi = new Rect((int) (x), (int) (y), (int) (w),
                    (int) (h));

            plateImage = new Mat(roi.size(), originImageOnAsy.type());

            plateImage = originImageOnAsy.submat(roi); //docinanie tablic z całego obrazu

            Mat plateImageResized = new Mat();

            Imgproc.resize(plateImage, plateImageResized, new Size(680,
                    492)); //dlaczego do rozmiaru 680, 492? - zwiekszone zdjecie i zwężone

//            Mat plateImageGrey = new Mat();

//            Imgproc.cvtColor(plateImageResized, plateImageGrey,
//                    Imgproc.COLOR_BGR2GRAY, 1);
            Imgproc.medianBlur(plateImageResized, plateImageResized, 1); //wygładzenie obrazu, "wyczyszczenie" w
            // celu wyodrebnienia głownych kolorów
            Imgproc.adaptiveThreshold(plateImageResized, plateImageResized,
                    255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY, 85, 5); //binaryzacja obrazu w skali szarości - pięknie zbinaryzowane - OK

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); //rozpoznane kontury, vector of points

            Mat hierarchy = new Mat(plateImageResized.rows(),
                    plateImageResized.cols(), CvType.CV_8UC1,
                    new Scalar(0));

            Imgproc.findContours(plateImageResized, contours, hierarchy,
                    Imgproc.CHAIN_APPROX_SIMPLE, Imgproc.RETR_LIST);

            String recognizedText = "";
            timeRequired = System.currentTimeMillis() - start;
            Log.e(TAG, "Time for find countour: " + timeRequired);
            Log.e(TAG, "Start loop!!!" + contours.size());
            start = System.currentTimeMillis();

            for (int i = 0; i < contours.size(); i++) {
                List<Point> goodpoints = new ArrayList<Point>();
                Mat contour = contours.get(i);
                int num = (int) contour.total(); //return the number of array elements
                int buff[] = new int[num * 2]; // [x1, y1, x2, y2, ...]
                contour.get(0, 0, buff);
                for (int q = 0; q < num * 2; q = q + 2) {
                    goodpoints.add(new Point(buff[q], buff[q + 1]));
                }

                MatOfPoint points = new MatOfPoint();
                points.fromList(goodpoints);
                Rect boundingRect = Imgproc.boundingRect(points);

                if (((boundingRect.height / boundingRect.width) >= 1.5)
                        && ((boundingRect.height / boundingRect.width) <= 3.0)
                        && ((boundingRect.height * boundingRect.width) >= 5000)) {

                    int cx = boundingRect.x + (boundingRect.width / 2);
                    int cy = boundingRect.y + (boundingRect.height / 2);

                    Point centroid = new Point(cx, cy);

                    if (centroid.y >= 120 && centroid.y <= 400
                            && centroid.x >= 100 && centroid.x <= 590) {

                        int calWidth = (boundingRect.width + 5)
                                - (boundingRect.width + 5) % 4;

                        Rect cr = new Rect(boundingRect.x,
                                boundingRect.y, calWidth,
                                boundingRect.height);

                        Mat charImage = new Mat(
                                cr.size(),
                                plateImageResized.type());

                        charImage = plateImageResized.submat(cr);


                        //dopisane
                        Bitmap imageBitmap = Bitmap.createBitmap(
                                charImage.width(),
                                charImage.height(),
                                Bitmap.Config.ARGB_8888);

                        org.opencv.android.Utils.matToBitmap(
                                charImage, imageBitmap);

                        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "ANPR/"); //sprawdzenie wyciętych liter
                        ImageFormatConversion.saveBitmapAsJpegFile(imageBitmap, folder);


                        Mat charImageGrey = new Mat(charImage.size(),
                                charImage.type());
//                        Imgproc.cvtColor(charImage, charImageGrey,
//                                Imgproc.COLOR_BGR2GRAY, 1);

                        Imgproc.adaptiveThreshold(charImageGrey,
                                charImageGrey, 255,
                                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                                Imgproc.THRESH_BINARY, 85, 5);

                        Bitmap charImageBitmap = Bitmap.createBitmap(
                                charImageGrey.width(),
                                charImageGrey.height(),
                                Bitmap.Config.ARGB_8888);

                        //possible characters
                        org.opencv.android.Utils.matToBitmap(
                                charImageGrey, charImageBitmap);

//                        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "ANPR/"); //sprawdzenie wyciętych liter
//                        ImageFormatConversion.saveBitmapAsJpegFile(charImageBitmap, folder);

                        tempBitmap = new BitmapWithCentroid(
                                charImageBitmap, centroid);
                        charList.add(tempBitmap);
                    }
                }
                // }
            }

            timeRequired = System.currentTimeMillis() - start;
            Log.e(TAG, "Passed the loop");
            Log.e(TAG, "Time for OCR: " + timeRequired);

            start = System.currentTimeMillis();
            Collections.sort(charList);

            SampleData data = new SampleData('?', DOWNSAMPLE_WIDTH,
                    DOWNSAMPLE_HEIGHT);

            for (int index = 0; index < charList.size(); index++) {
                newBitmap = charList.get(index).getBitmap();

                final int wi = newBitmap.getWidth();
                final int he = newBitmap.getHeight();

                pixelMap = new int[newBitmap.getHeight()
                        * newBitmap.getWidth()];
                newBitmap.getPixels(pixelMap, 0, newBitmap.getWidth(),
                        0, 0, newBitmap.getWidth(),
                        newBitmap.getHeight());

                findBounds(wi, he);

                ratioX = (double) (downSampleRight - downSampleLeft)
                        / (double) data.getWidth();
                ratioY = (double) (downSampleBottom - downSampleTop)
                        / (double) data.getHeight();

                for (int yy = 0; yy < data.getHeight(); yy++) {
                    for (int xx = 0; xx < data.getWidth(); xx++) {
                        if (downSampleRegion(xx, yy)) {
                            data.setData(xx, yy, true);
                        } else {
                            data.setData(xx, yy, false);
                        }
                    }
                }

                final double input[] = new double[20 * 50];
                int idx = 0;
                for (int yy = 0; yy < data.getHeight(); yy++) {
                    for (int xx = 0; xx < data.getWidth(); xx++) {
                        input[idx++] = data.getData(xx, yy) ? 0.5
                                : -0.5;
                    }
                }

                double normfac[] = new double[1];
                double synth[] = new double[1];

                Assert.assertNotNull(input);
                if(input == null){
                    Log.e(TAG, "Input array is Null");

                }

                int best = net.winner(input, normfac, synth); //rozpoznawanie litery z jakis danych

                recognizedText += net.getMap()[best];
                Log.e(TAG, "Plate number:" + recognizedText);
            }

            recognizedText = utils.formatPlateNumber(recognizedText);

            if (TextUtils.isEmpty(result))
                result = recognizedText;
            else
                result += "\n" + recognizedText;

            timeRequired = System.currentTimeMillis() - start;
            Log.e(TAG, "Time: " + timeRequired);
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String aResult) {
        isRunningTask = false;
        if (!TextUtils.isEmpty(aResult))
            isFail = false;
        else
            isFail = true;
        listener.updateResult(aResult);
    }

    /**
     * This method is called to automatically crop the image so that whitespace
     * is removed.
     *
     * @param w
     *            The width of the image.
     * @param h
     *            The height of the image
     */
    protected void findBounds(final int w, final int h) {
        // top line
        for (int y = 0; y < h; y++) {
            if (!hLineClear(y)) {
                this.downSampleTop = y;
                break;
            }

        }
        // bottom line
        for (int y = h - 1; y >= 0; y--) {
            if (!hLineClear(y)) {
                this.downSampleBottom = y;
                break;
            }
        }
        // left line
        for (int x = 0; x < w; x++) {
            if (!vLineClear(x)) {
                this.downSampleLeft = x;
                break;
            }
        }

        // right line
        for (int x = w - 1; x >= 0; x--) {
            if (!vLineClear(x)) {
                this.downSampleRight = x;
                break;
            }
        }
    }

    protected boolean downSampleRegion(final int x, final int y) {
        final int w = this.newBitmap.getWidth();
        final int startX = (int) (this.downSampleLeft + (x * this.ratioX));
        final int startY = (int) (this.downSampleTop + (y * this.ratioY));
        final int endX = (int) (startX + this.ratioX);
        final int endY = (int) (startY + this.ratioY);

        for (int yy = startY; yy <= endY; yy++) {
            for (int xx = startX; xx <= endX; xx++) {
                final int loc = xx + (yy * w);

                if (this.pixelMap[loc] != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This method is called internally to see if there are any pixels in the
     * given scan line. This method is used to perform autocropping.
     *
     * @param y
     *            The horizontal line to scan.
     * @return True if there were any pixels in this horizontal line.
     */
    protected boolean hLineClear(final int y) {
        final int w = this.newBitmap.getWidth();
        for (int i = 0; i < w; i++) {
            if (this.pixelMap[(y * w) + i] != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called to determine ....
     *
     * @param x
     *            The vertical line to scan.
     * @return True if there are any pixels in the specified vertical line.
     */
    protected boolean vLineClear(final int x) {
        final int w = this.newBitmap.getWidth();
        final int h = this.newBitmap.getHeight();
        for (int i = 0; i < h; i++) {
            if (this.pixelMap[(i * w) + x] != -1) {
                return false;
            }
        }
        return true;
    }
}