package com.example.ejwon.anpr.fileIO;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class ReadJsonFile {

    public static String loadedJsonData;

    public static void ReadFileToMemory() {
        try {

            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "RecognizedPlate"); //tablica
            if (!folder.exists()) {
                folder.mkdir();
            }
            File yourFile = new File(folder, "tablice.json");
            FileInputStream stream = new FileInputStream(yourFile);
            loadedJsonData = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                loadedJsonData = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}