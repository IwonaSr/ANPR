package com.example.ejwon.anpr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    ImageView imageView;
    public Context context = this;
    public DrawView drawView;
    public PlateView plateView;
    private static final String TAG = "MainActivity.java";
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Draw rectangle
//        setContentView(new DrawView(this));
        layout = (RelativeLayout) findViewById(R.id.mainFrame);

        //Display Number Plate Image
//        imageView = (ImageView) findViewById(R.id.imageView);
//        this.readImageFromResources();
//        plateView = new PlateView(this);
//        layout.addView(plateView, 1);

        Boolean checkOpenCV = OpenCVLoader.initAsync(
                OpenCVLoader.OPENCV_VERSION_3_0_0,
                getApplicationContext(),
                mOpenCVCallBack);

        if(checkOpenCV)
        {
            try {

            } catch (Exception e1) {
                Log.e("MissingOpenCVManager",e1.toString());
            }
        }

        float mRelativePlateSize = 0.2f;


//        Bitmap bitmap= BitmapFactory.decodeResource(this.getResources(), R.drawable.tablica);
//        Bitmap mutBitmap = Bitmap.createBitmap(200, 400,bitmap.getConfig());
//        Canvas canvas = new Canvas(mutBitmap);
//        DrawView drawView = new DrawView(this);
//        drawView.draw(canvas);
//        drawView.setImageBitmap(mutBitmap);
//        drawView.postInvalidate();

    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //Load Cascade Classifier
                    loadCascadeClassifier();
                    Bitmap b = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    int w = b.getWidth();
                    int h = b.getHeight();

                    plateView.processImage(convertImageToByteArray(b), w, h);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    private void readImageFromResources() {
        imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tablica));
    }

    private void loadCascadeClassifier() {

        try {
            // Load Haar training result file from application resources
            // This file from opencv_traincascade tool.
            // Load res/cascade-europe.xml file
            InputStream is = getResources().openRawResource(R.raw.europe);

            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "europe.xml"); // Load XML file according to R.raw.cascade
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
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from "
                        + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();
        }catch(IOException e){
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }

    }
    private byte[] convertImageToByteArray(Bitmap bitmap){
//        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.tablica);
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); // what 90 does ??
        byte[] image=stream.toByteArray();
        return image;
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Called onResume");
        super.onResume();

        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallBack);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}


