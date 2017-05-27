package com.example.ejwon.anpr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by Ejwon on 2017-05-13.
 */
public final class ImageFormatConversion {

    private static final String TAG = "Save Bitmap as Image";

    //http://answers.opencv.org/question/61628/android-camera2-yuv-to-rgb-conversion-turns-out-green/?answer=100322#post-id-100322
    public static Mat convertYuv420888ToMat(Image image, boolean isGreyOnly) {
        int width = image.getWidth();
        int height = image.getHeight();
        Mat yuvMat;
        Mat grayMat = new Mat(height, width, CvType.CV_8UC1);;

        Image.Plane yPlane = image.getPlanes()[0];
        int ySize = yPlane.getBuffer().remaining();

        if (isGreyOnly) {
            byte[] data = new byte[ySize];
            yPlane.getBuffer().get(data, 0, ySize);

            Mat mYuv = new Mat(height + height / 2, width, CvType.CV_8UC1);
            mYuv.put(0, 0, data);
            Imgproc.cvtColor(mYuv, grayMat, Imgproc.COLOR_YUV420sp2GRAY);
            //Rotate YUV image(grayscale) because
            grayMat = rotateMat(grayMat, 90);
            return grayMat;
        }

        //PONIŻEJ KOD JEST ZLY
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];

        // be aware that this size does not include the padding at the end, if there is any
        // (e.g. if pixel stride is 2 the size is ySize / 2 - 1)
        int uSize = uPlane.getBuffer().remaining();
        int vSize = vPlane.getBuffer().remaining();
        byte[] data = new byte[ySize + (ySize / 2)];

        yPlane.getBuffer().get(data, 0, ySize);
        ByteBuffer ub = uPlane.getBuffer();
        ByteBuffer vb = vPlane.getBuffer();

        int uvPixelStride = uPlane.getPixelStride(); //stride guaranteed to be the same for u and v planes
        if (uvPixelStride == 1) {
            uPlane.getBuffer().get(data, ySize, uSize);
            vPlane.getBuffer().get(data, ySize + uSize, vSize);

            yuvMat = new Mat(height + (height / 2), width, CvType.CV_8UC1);
            yuvMat.put(0, 0, data);

            Mat mGray = new Mat(height, width, CvType.CV_8UC1); // 8 bit 1 kanał szare
            Imgproc.cvtColor(yuvMat, mGray, Imgproc.COLOR_YUV420sp2GRAY);
//                Mat rgbMat = new Mat(height, width, CvType.CV_8UC3);
//                Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420, 3);
            yuvMat.release();
            return grayMat;
        }

        // if pixel stride is 2 there is padding between each pixel
        // converting it to NV21 by filling the gaps of the v plane with the u values
        vb.get(data, ySize, vSize);
        for (int i = 0; i < uSize; i += 2) {
            data[ySize + i + 1] = ub.get(i);
        }

        yuvMat = new Mat(height + (height / 2), width, CvType.CV_8UC1);
        yuvMat.put(0, 0, data);

        //CONVERT TO RGB
//            Mat rgbMat = new Mat(height, width, CvType.CV_8UC3);
//            Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21, 3);
//            yuvMat.release();
        Mat mGray = new Mat(height, width, CvType.CV_8UC1); // 8 bit 1 kanał szare
        Imgproc.cvtColor(yuvMat, mGray, Imgproc.COLOR_YUV420sp2GRAY);

        return grayMat;
    }

    public static Mat rotateMat(Mat src, int angle){

        Mat dst = new Mat();
        if(angle == 270 || angle == -90){
            // Rotate clockwise 270 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 0);
        }else if(angle == 180 || angle == -180){
            // Rotate clockwise 180 degrees
            Core.flip(src, dst, -1);
        }else if(angle == 90 || angle == -270){
            // Rotate clockwise 90 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 1);
        }else if(angle == 360 || angle == 0 || angle == -360){
                src.copyTo(dst);
        }
        return dst;
    }

    public static byte[] getAllBytesFromYUVImage(Image mImage) {
        ByteBuffer buffer;
        byte[] bytes = null;
        for (int i = 0; i < 3; i++) {
            buffer = mImage.getPlanes()[i].getBuffer();
            bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
            buffer.get(bytes); // copies image from
        }
        return bytes;
    }
    public static byte[] getByteFromImage(Image image) {
        Image.Plane yPlane = image.getPlanes()[0];
        int ySize = yPlane.getBuffer().remaining();
        byte[] data = new byte[ySize];
        yPlane.getBuffer().get(data, 0, ySize);
        return data;
    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmapFromBytes(byte[] data) {
//        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bmp;
    }

    public static Bitmap getBitmapFromMat(Mat tmp) {

        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tmp, bmp);
            return bmp;
        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }

        return bmp;
    }


    public static void saveBitmapAsJpegFile(Bitmap bm, File myDir) {

        Random generator = new Random();
        int n = 10000;

        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
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

    private byte[] toByteArray(Image image, File destination) {

        Image.Plane yPlane = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];

        int ySize = yPlane.getBuffer().remaining();

        // be aware that this size does not include the padding at the end, if there is any
        // (e.g. if pixel stride is 2 the size is ySize / 2 - 1)
        int uSize = uPlane.getBuffer().remaining();
        int vSize = vPlane.getBuffer().remaining();

        byte[] data = new byte[ySize + (ySize / 2)];

        yPlane.getBuffer().get(data, 0, ySize);

        ByteBuffer ub = uPlane.getBuffer();
        ByteBuffer vb = vPlane.getBuffer();

        int uvPixelStride = uPlane.getPixelStride(); //stride guaranteed to be the same for u and v planes

        if (uvPixelStride == 1) {

            uPlane.getBuffer().get(data, ySize, uSize);
            vPlane.getBuffer().get(data, ySize + uSize, vSize);
        } else {

            // if pixel stride is 2 there is padding between each pixel
            // converting it to NV21 by filling the gaps of the v plane with the u values
            vb.get(data, ySize, vSize);
            for (int i = 0; i < uSize; i += 2) {
                data[ySize + i + 1] = ub.get(i);
            }
        }

        return data;
    }

    public static int[] getRGBIntFromPlanes(int mHeight, Image.Plane[] planes) {

        ByteBuffer yPlane = planes[0].getBuffer();
        ByteBuffer uPlane = planes[1].getBuffer();       // The U (Cr) plane
        ByteBuffer vPlane = planes[2].getBuffer();       // the V (Cb) plane
        int[] mRgbBuffer = null;
        int bufferIndex = 0;
        final int total = yPlane.capacity();
        final int uvCapacity = uPlane.capacity();
        final int width = planes[0].getRowStride();

        int yPos = 0;
        for (int i = 0; i < mHeight; i++) {
            int uvPos = (i >> 1) * width;

            for (int j = 0; j < width; j++) {
                if (uvPos >= uvCapacity - 1)
                    break;
                if (yPos >= total)
                    break;

                final int y1 = yPlane.get(yPos++) & 0xff;

                /*
                  The ordering of the u (Cb) and v (Cr) bytes inside the planes is a bit strange.
                  The _first_ byte of the u-plane and the _second_ byte of the v-plane build the
                  u/v pair and belong to the first two pixels (y-bytes), thus usual YUV 420 behavior.
                  What the Android devs did here: just copy the interleaved NV21 data to two planes
                  but keep the offset of the interleaving.
                 */
                final int u = (uPlane.get(uvPos) & 0xff) - 128;
                final int v = (vPlane.get(uvPos + 1) & 0xff) - 128;
                if ((j & 1) == 1) {
                    uvPos += 2;
                }

                // This is the integer variant to convert YCbCr to RGB, NTSC values.
                // formulae found at
                // https://software.intel.com/en-us/android/articles/trusted-tools-in-the-new-android-world-optimization-techniques-from-intel-sse-intrinsics-to
                // and on StackOverflow etc.
                final int y1192 = 1192 * y1;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                r = (r < 0) ? 0 : ((r > 262143) ? 262143 : r);
                g = (g < 0) ? 0 : ((g > 262143) ? 262143 : g);
                b = (b < 0) ? 0 : ((b > 262143) ? 262143 : b);

                mRgbBuffer[bufferIndex++] = ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return mRgbBuffer;
    }
}

