package com.example.ejwon.anpr.common;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Ejwon on 2017-09-17.
 */
public class JsonIterator {

    private static final String TAG = "JsonIterator.java";

    public static String iterateJsonData(String jsonStr, String result) throws JSONException {
        String value = "";
        JSONObject jsonObj = new JSONObject(jsonStr);
        String tablica = jsonObj.toString();
//            Log.d("Tablice ", tablica); //OK

        Iterator<String> iter = jsonObj.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            Log.d(TAG, "Key " + key);

            try {
                if (key.equals(result)) {
                    value = (String) jsonObj.get(key);
                    Log.d(TAG, "Value " + result);
                    break;
                } else {

                    Log.d(TAG, "Key " + result + " not found in json");

                }
            } catch (JSONException e) {
                // Something went wrong!
                Log.d("Iterate json data", " JSONException");
                e.printStackTrace();
            }
        }
        return value;
    }
}
