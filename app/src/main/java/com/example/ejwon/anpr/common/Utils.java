package com.example.ejwon.anpr.common;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.ejwon.anpr.interfaces.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
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

    public Result lookingForPlate(Result result) {
        String number = "";
        String recognizedTown = "";
        String recInfo= "";
        String recognizedPlate = result.getRecognizedNumber();
        Log.d("RecognizedPlate: ", recognizedPlate);
        int i = 0;
        int ct = 0;
        Index index =  new Index();
        if (!recognizedPlate.isEmpty()) {
            i = i + 1;
            Log.d("Index: ", "" + i);

            String districtNumber = recognizedPlate.substring(0, 2);
            Log.e(TAG, "Plate: " + districtNumber);
            recognizedTown = ReadFile(districtNumber);
            Log.e(TAG, "Town: " + recognizedTown);
            result.setRecognizedTown(recognizedTown);

            if (!recognizedTown.isEmpty()) {
                Log.d("Dupa: ", "" + ct);
                if (recognizedTown.contains("Mys")) {
                    ct = ct + 1;
                    index.setTown(ct);
                    Log.d("TownInside: ", "" + ct);
                }
//            String time = result.getAllTimes().timeToDisplay();
//            recInfo = "(" + i + ")" + recognizedPlate + ":" + recognizedTown + ":ct:" + ct + time + ", ";
//            result.setNumberIteration(i);
//            result.setNumberTown(ct);
            }

            index.setNumber(i);
            result.setIndex(index);
        }

        return result;
    }

    public String ReadFile (String result){
        String TAG = "readJsonFile";
        String value = "";
        try {

            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "RecognizedPlate"); //tablica
            if(!folder.exists()){
                folder.mkdir();
            }
            File yourFile = new File(folder, "tablice.json");
            FileInputStream stream = new FileInputStream(yourFile);
            String jsonStr = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                stream.close();
            }
            JSONObject jsonObj = new JSONObject(jsonStr);
            String tablica = jsonObj.toString();
//            Log.d("Tablice ", tablica); //OK

            Iterator<String> iter = jsonObj.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                Log.d(TAG, "Key " + key);

                try {
                    if(key.equals(result)){
                        value = (String) jsonObj.get(key);
                        Log.d(TAG, "Value " + result);
                    }
                    else{

                        Log.d(TAG, "Key " + result + " not found in json");

                    }
                } catch (JSONException e) {
                    // Something went wrong!
                    Log.d("ReadFile", " JSONException");
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(value.isEmpty())
        {
            value = "";
        }
        return value;
    }

    public String formatPlateNumber(String source)
    {
        String result = source;

        if(source.length() == 8) {
            Log.e(TAG, "osiem: " + 8);
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2)) +  source.substring(2, 4) +  correctNumber(source.substring(4, 8));
        }
        else if (source.length() == 9) {
            Log.e(TAG, "dziewiec: " + 9);
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2))  + source.substring(2, 4) + correctNumber(source.substring(4, 7)) + "." + correctNumber((source.substring(7, 9)));
        }else if (source.length() == 7) {
            Log.e(TAG, "siedem: " + 7);
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2)) +  source.substring(2, 4) +  correctNumber(source.substring(4, 7));
        }
        else if (source.length() == 6)
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2)) +  source.substring(2, 4) +  correctNumber(source.substring(4, 6));
        else if (source.length() == 5)
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2)) + source.substring(2, 4) +  correctNumber(source.substring(4, 5));
        else if (source.length() == 4)
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2)) + source.substring(2, 4);
        else if (source.length() == 3)
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2)) + source.substring(3);
        else if (source.length() == 2)
            result = correctFirstLetter(source.substring(0,1)) + correctNumberForPoland(source.substring(1, 2));
        else
            result = "";

        return result;
    }

    public String formatPlateNumberPoland(String source)
    {
        String result = source;

        if(source.length() == 8)
            result = correctNumberForPoland(source.substring(0, 2)) + "-" + source.substring(2, 4) + " " + correctNumber(source.substring(4, 7));
        else if (source.length() == 9)
            result = correctNumberForPoland(source.substring(0, 2)) + "-" + source.substring(2, 4) + " " + correctNumber(source.substring(4, 7)) + "." + correctNumber((source.substring(7, 9)));
        else if (source.length() == 7)
            result = correctNumberForPoland(source.substring(0, 2)) + "-" + source.substring(2, 4) + " " + correctNumber(source.substring(4, 7));
        else
            result = "";

        return result;
    }


    public String correctNumberForPoland(String source)
    {
        char[] sourceArray = source.toCharArray();
        for(int index = 0; index < sourceArray.length; index++)
        {
            if(sourceArray[index] == '2')
                sourceArray[index] = 'Z';
            else if (sourceArray[index] == '5')
                sourceArray[index] = 'S';
            else if (sourceArray[index] == '0')
                sourceArray[index] = 'O';
            else if (sourceArray[index] == '9')
                sourceArray[index] = 'P';
            else if (sourceArray[index] == '6')
                sourceArray[index] = 'G';
            else if (sourceArray[index] == '1')
                sourceArray[index] = 'I';
            else if (sourceArray[index] == '8')
                sourceArray[index] = 'B';
            else if (sourceArray[index] == '4')
                sourceArray[index] = 'L';
        }

        return String.valueOf(sourceArray);
    }

    public String correctFirstLetter(String source)
    {
        String firstLetter = correctNumberForPoland(source);
        if( firstLetter.contains("M")){
            firstLetter = "W";
        }
        return firstLetter;
    }

    public String correctNumber(String source)
    {
        char[] sourceArray = source.toCharArray();
        for(int index = 0; index < sourceArray.length; index++)
        {               Log.e(TAG, "DUPA" + sourceArray[index]);

            if(sourceArray[index] == 'Z') {
                sourceArray[index] = '2';
                Log.e(TAG, "Z jako 2: " + sourceArray[index]);
            }
            else if (sourceArray[index] == 'S') {
                sourceArray[index] = '5';
                Log.e(TAG, "S jako 5: " + sourceArray[index]);
            }
            else if (sourceArray[index] == 'D') {
                sourceArray[index] = '0';
                Log.e(TAG, "D jako 0: " + sourceArray[index]);
            }
//            else if (sourceArray[index] == '0')
//                sourceArray[index] = 'D';
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



}