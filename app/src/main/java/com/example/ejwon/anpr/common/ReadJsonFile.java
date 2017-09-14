package com.example.ejwon.anpr.common;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class ReadJsonFile {
    public static String readJsonFromAsset (Context context){

        String json = null;
        try {

            InputStream is = context.getAssets().open("file_name.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }






}