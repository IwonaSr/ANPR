package com.example.ejwon.anpr.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.example.ejwon.anpr.ImageFormatConversion;
import com.example.ejwon.anpr.interfaces.Constants;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

public class Utils {

    Context mContext;
    private static final String TAG = "Utils.java";

    public Utils(Context context) {
        this.mContext = context;
    }

    public double convertSpeed(double speed) {
        return ((speed * Constants.HOUR_MULTIPLIER) * Constants.UNIT_MULTIPLIERS);
    }

    public double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();

        return value;
    }

    public String formatPlateNumber(String source)
    {
        String result = source;

        if(source.length() == 8)
            result = correctNumber(source.substring(0, 2)) + "-" + source.substring(2, 4) + " " + correctNumber(source.substring(4, 8));
        else if (source.length() == 9)
            result = correctNumber(source.substring(0, 2)) + "-" + source.substring(2, 4) + " " + correctNumber(source.substring(4, 7)) + "." + correctNumber((source.substring(7, 9)));
        else
            result = "";
        return result;
    }

    public String correctNumber(String source)
    {
        char[] sourceArray = source.toCharArray();
        for(int index = 0; index < sourceArray.length; index++)
        {
            if(sourceArray[index] == 'Z')
                sourceArray[index] = '2';
            else if (sourceArray[index] == 'S')
                sourceArray[index] = '5';
            else if (sourceArray[index] == 'D')
                sourceArray[index] = '0';
        }

        return String.valueOf(sourceArray);
    }

    public boolean isNewPlate(List<Point> platePointList, Point platePoint)
    {
        boolean result = true;

        Iterator<Point> iterator = platePointList.iterator();

        while(iterator.hasNext())
        {
            Point currentPoint = iterator.next();
            int distance = distanceOfPoint(currentPoint, platePoint);
            if(distance <= 10)
            {
                result = false;
                break;
            }
        }

        return result;
    }

    private int distanceOfPoint(Point firstPoint, Point secondPoint)
    {
        int result = (int) Math.sqrt(Math.pow(firstPoint.x - secondPoint.x, 2) + Math.pow(firstPoint.y - secondPoint.y, 2));
        return result;
    }

    public void saveImage(Bitmap source, String name)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        source.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        //you can create a new file name "test.jpg" in sdcard folder.
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "test" + name + ".png");

        //write the bytes in file
        FileOutputStream fo;
        try {
            f.createNewFile();
            fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveImageToFile(Mat plateImageResized, String locationFolder){

        Bitmap imageBitmap = Bitmap.createBitmap(
                plateImageResized.width(),
                plateImageResized.height(),
                Bitmap.Config.ARGB_8888);

        org.opencv.android.Utils.matToBitmap(
                plateImageResized, imageBitmap);

//        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "ANPR_plate2/"); //tablica
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + locationFolder); //tablica
        folder.mkdir();
        ImageFormatConversion.saveBitmapAsJpegFile(imageBitmap, folder);
    }

    public static void saveRecognizedTextToFile(String text){
        File file = null;
        Boolean write_successful = false;
        try {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "RecognizedPlate"); //tablica
        if(!folder.exists()){
            folder.mkdir();
        }
        file = new File(folder, "rexognizedPlate.txt");
        FileWriter filewriter = new FileWriter(file, true);
        BufferedWriter out = new BufferedWriter(filewriter);
            text = text + ", ";
        out.write(text);
        out.close();
        write_successful = true;

        } catch (IOException e) {
            Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());
            write_successful = false;
            e.printStackTrace();
        }

        Log.e(TAG, "Status of txt: " + write_successful);
    }


}