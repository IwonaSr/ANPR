package com.example.ejwon.anpr.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Ejwon on 2017-09-10.
 */
public class ReadWriteImageFile {

    private static final String TAG = "ReadWriteImageFile";
    public static String fname = "";

    public void saveImage(Bitmap source, String name) {
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

    public static void saveImageToFile(Mat plateImageResized, String locationFolder) {

        Bitmap imageBitmap = Bitmap.createBitmap(
                plateImageResized.width(),
                plateImageResized.height(),
                Bitmap.Config.ARGB_8888);

        org.opencv.android.Utils.matToBitmap(plateImageResized, imageBitmap);
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + locationFolder); //tablica
        folder.mkdir();
        ReadWriteImageFile.saveBitmapAsJpegFile(imageBitmap, folder);
    }

    public static void saveResultToFile(Result result){
        File file = null;
        Boolean write_successful = false;
        String savedResult = "";
        savedResult = result.displayResult();
//        Log.d("SavedResult: ", savedResult);
        if(!savedResult.isEmpty()) {

            try {
                File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "RecognizedPlate"); //tablica
                if (!folder.exists()) {
                    folder.mkdir();
                }
                file = new File(folder, "TEST_plate.txt");
                FileWriter filewriter = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(filewriter);

                out.write(savedResult);
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

    public static Result saveRecognizedTownToFile(Result result, String text, int i, int ct) {
        File file = null;
        Boolean write_successful = false;
        String number = "";
        String recognizedTown = "";
        String recNumber = "";
//        Result result = new Result();

        //all number
        number = text;
        if (!text.isEmpty()) {
            i = i + 1;

            String districtNumber = number.substring(0, 2);
            Log.e(TAG, "Plate: " + districtNumber);
//            recognizedTown = ReadJsonFile.ReadFile(districtNumber);
            Log.e(TAG, "Town: " + recognizedTown);

            if (!recognizedTown.isEmpty())
                if (recognizedTown.contains("Mysłowice")) {
                    ct = ct + 1;
                }

            recNumber = "(" + i + ")" + text + ":" + recognizedTown + ":ct:" + ct + result.getAllTimes().timeToDisplay() + ", ";
//            result.setNumberIteration(i);
//            result.setNumberTown(ct);

            try {
                File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "RecognizedPlate"); //tablica
                if (!folder.exists()) {
                    folder.mkdir();
                }
                file = new File(folder, "TEST_plate.txt");
                FileWriter filewriter = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(filewriter);

                out.write(recNumber);
                out.close();
                write_successful = true;

            } catch (IOException e) {
                Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());
                write_successful = false;
                e.printStackTrace();
            }
//            Log.e(TAG, "Status of txt: " + write_successful);
        }

        return result;
    }

    public static void saveRecognizedTextToFile(String text, int i) {
        File file = null;
        Boolean write_successful = false;
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "RecognizedPlate"); //tablica
            if (!folder.exists()) {
                folder.mkdir();
            }
            file = new File(folder, "rexognizedPlateTest2.txt");
            FileWriter filewriter = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(filewriter);
            text = "(" + i + ") " + text + ", ";
            i = i + 1;
            out.write(text);
            Log.e(TAG, "text: " + text + "i: " + i);
            out.close();
            write_successful = true;

        } catch (IOException e) {
            Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());
            write_successful = false;
            e.printStackTrace();
        }

        Log.e(TAG, "Status of txt: " + write_successful);
    }

    public static void saveBitmapAsJpegFile(Bitmap bm, File myDir) {

        Random generator = new Random();
        int n = 10000;

        n = generator.nextInt(n);
        fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        Log.i(TAG, "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Mat readJpegImageToMat() {

        String path = "storage/emulated/0/ANPR_znaki2/" + fname;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        int type = mat.type();
        Log.d(TAG, "type of readed Mat from File: " + type);
        return mat;
    }
}
