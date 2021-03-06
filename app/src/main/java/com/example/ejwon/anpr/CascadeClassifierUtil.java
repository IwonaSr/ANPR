package com.example.ejwon.anpr;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Ejwon on 2017-05-20.
 */
public final class CascadeClassifierUtil {

    private static File mCascadeFile;
    private static final String TAG = "CascadeClass.java";

    public static CascadeClassifier loadCascadeClassifier(Resources resources, Context context) {
        CascadeClassifier mJavaDetector = null;
        try {
            // Load Haar training result file from application resources
            // This file from opencv_traincascade tool.
            // Load res/cascade-europe.xml file
            InputStream is = resources.openRawResource(R.raw.europe);

            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            Log.d(TAG, "cascadeDir: " + cascadeDir);

            mCascadeFile = new File(cascadeDir, "europe.xml"); // Load XML file according to R.raw.cascade
            if(mCascadeFile != null){
                Log.d(TAG, "mCascadeFile.exitst: " + mCascadeFile);

            }

            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(
                    mCascadeFile.getAbsolutePath());
            //new line added
            mJavaDetector.load(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from "
                        + mCascadeFile.getAbsolutePath());

//            cascadeDir.delete();
        }catch(IOException e){
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
        return mJavaDetector;

    }
}
