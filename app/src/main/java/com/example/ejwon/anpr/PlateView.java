package com.example.ejwon.anpr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ejwon.anpr.OCRRecognition.OCRRecognition;
import com.example.ejwon.anpr.common.Utils;
import com.example.ejwon.anpr.interfaces.OnTaskCompleted;
import com.hazuu.uitanpr.neural.KohonenNetwork;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ejwon on 2017-03-11.
 */
public class PlateView extends View implements OnTaskCompleted {

    Rect[] platesArray;
    private static final String TAG = "PlateView.java";
    List<Point> currentPlatePointList = new ArrayList<Point>();
    List<Rect> currentPlates = new ArrayList<Rect>();
    MatOfRect plates;
    Mat mGray;
    int number;
    Utils utils;
    List<Point> platePointList;
    public boolean isFail = false;
    KohonenNetwork net;
    public boolean isRunningTask = false;
    TextView resultOCR;

    // Preparing for storing plate region

    public PlateView(AndroidCameraApi context) {
        super(context);
        this.setWillNotDraw(false);
    }

//    public void setPlate(MatOfRect plates, int number, Utils utils,  List<Point> platePointList) {
    public void setUtils(Utils utils,  List<Point> platePointList) {
//        this.plates = plates;
//        this.number = number;
        this.utils = utils;
        this. platePointList = platePointList;
    }

    public void setPlate(MatOfRect plates) {
        this.plates = plates;
    }

    public void setNetwork(KohonenNetwork net) { this.net = net; }

    public void setGrayMat (Mat mGray) {
        this.mGray = mGray;
    }

    public void setResultOCR (TextView result) {
        this.resultOCR = result;
    }

    public TextView getResultOCR() {
        return resultOCR;
    }
//    public TextView getResultOCR () {
//        return this.resultOCR;
//    }


    public PlateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PlateView(AndroidCameraApi context, MatOfRect plates) {
//    public PlateView(AndroidCameraApi context, MatOfRect plates, int number) {
        super(context);
        plates = this.plates;
//        number = this.number;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(20);
        Log.d(TAG, "On draw");

        //wyświetlanie kwadratu
//        if (number != 0) {
//            Log.d(TAG, "Number: " + number);
//
//
//            float leftx = 100;
//            float topy = 100;
//            float rightx = 200;
//            float bottomy = 200;
//            canvas.drawRect(leftx, topy, rightx, bottomy, paint);
//
//        }


        if (plates != null) {

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            Log.d(TAG, "Plates is not null; " + plates);

            platesArray = plates.toArray();
            Log.d(TAG, "platesArray: " + platesArray);
//            Log.d(TAG, "platesArray.length: " + platesArray.length);

            boolean isHasNewPlate = false;
            currentPlates.clear();

            for (int i = 0; i < platesArray.length; i++) {
                int x = platesArray[i].x;
//                Log.d(TAG, "platesArray[i].x: " + x);

                int y = platesArray[i].y;
//                Log.d(TAG, "platesArray[i].y; " + y);

                int w = platesArray[i].width;
//                Log.d(TAG, "platesArray[i].width; " + w);

                int h = platesArray[i].height;
//                Log.d(TAG, "platesArray[i].heigh: " + h);


                // Draw a Green Rectangle surrounding the Number Plate !
                // Congratulations ! You found the plate area :-)
                canvas.drawRect(x, y, (x + w), (y + h), paint);
//                Log.i(TAG, "drawRect(x, y, (x + w), (y + h)" + x + ", " + y + ", " + (x + w) + ", " + (y + h));
                Log.i("Plate found"," Found a plate !!!");

                // isNewPlate?
                Point platePoint = new Point(platesArray[i].x,
                        platesArray[i].y);
                currentPlatePointList.add(platePoint);
                currentPlates.add(platesArray[i]);
                if (utils.isNewPlate(platePointList, platePoint)) {
                    isHasNewPlate = true;
                }
            }

            if (platesArray.length  > 0) {
                platePointList.clear();
                platePointList.addAll(currentPlatePointList);
            } else {
                platePointList.clear();
            }

            // If isHasNewPlate --> get sub images (ROI) --> Add to Adapter
            // (from currentPlates)
            if ((isHasNewPlate || isFail) && !isRunningTask) {
                Log.e(TAG, "START DoOCR task!!!!");
                new OCRRecognition(currentPlates, mGray, isRunningTask, this, net, utils).execute();
            }

        }

    }

    @Override
    public void updateResult(String result) {
        resultOCR.setText(result);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String toFile = "Date: " + timeStamp + "\n";
        toFile += "NP: " + result + "\n\n\n";
        if(result!=null && !TextUtils.isEmpty(result) && result!=""){
            FileWriter f;
            try{
                f = new FileWriter(Environment.getExternalStorageDirectory() + "/IW-ANPR.txt", true);
                Log.e("PlateView", "Save recognized plates");
                f.write(toFile);
                f.flush();
                f.close();
            }catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

}
