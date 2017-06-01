package com.example.ejwon.anpr.imageLoader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import com.example.ejwon.anpr.AndroidCameraApi;
import com.example.ejwon.anpr.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Ejwon on 2017-05-30.
 */
public final class ImageLoader {

    public static void readImageFromFile(AndroidCameraApi activity){
        File imgFile = new File(Environment.getExternalStorageDirectory() + File.separator + "ANPR/tablica.png");

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView myImage = (ImageView) activity.findViewById(R.id.imageView);

            myImage.setImageBitmap(myBitmap);
        }
    }


    public static Bitmap loadImageAsBitmap(){
        String path = Environment.getExternalStorageDirectory() + File.separator + "ANPR/tablica6.jpg";
        File imgFile = new File(path);
        Bitmap bitmap = null;
        if(imgFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(imgFile), null, options);
                assert bitmap != null;
                //        selected_photo.setImageBitmap(bitmap);
                return bitmap;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

}
