package com.example.ejwon.anpr;

import android.media.Image;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Created by Ejwon on 2017-05-13.
 */
public final class ImageFormatConversion {

    //http://answers.opencv.org/question/61628/android-camera2-yuv-to-rgb-conversion-turns-out-green/?answer=100322#post-id-100322
    public static Mat convertYuv420888ToMat(Image image, boolean isGreyOnly){
        int width = image.getWidth();
        int height = image.getHeight();
        Mat yuvMat;

        Image.Plane yPlane = image.getPlanes()[0];
        int ySize = yPlane.getBuffer().remaining();

            if (isGreyOnly) {
                byte[] data = new byte[ySize];
                yPlane.getBuffer().get(data, 0, ySize);

                Mat greyMat = new Mat(height, width, CvType.CV_8UC1);
                greyMat.put(0, 0, data);

                return greyMat;
            }

            Image.Plane uPlane = image.getPlanes()[1];
            Image.Plane vPlane = image.getPlanes()[2];

            // be aware that this size does not include the padding at the end, if there is any
            // (e.g. if pixel stride is 2 the size is ySize / 2 - 1)
            int uSize = uPlane.getBuffer().remaining();
            int vSize = vPlane.getBuffer().remaining();
            byte[] data = new byte[ySize + (ySize/2)];

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
                return mGray;
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

            return yuvMat;
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

        byte[] data = new byte[ySize + (ySize/2)];

        yPlane.getBuffer().get(data, 0, ySize);

        ByteBuffer ub = uPlane.getBuffer();
        ByteBuffer vb = vPlane.getBuffer();

        int uvPixelStride = uPlane.getPixelStride(); //stride guaranteed to be the same for u and v planes

        if (uvPixelStride == 1) {

            uPlane.getBuffer().get(data, ySize, uSize);
            vPlane.getBuffer().get(data, ySize + uSize, vSize);
        }
        else {

            // if pixel stride is 2 there is padding between each pixel
            // converting it to NV21 by filling the gaps of the v plane with the u values
            vb.get(data, ySize, vSize);
            for (int i = 0; i < uSize; i += 2) {
                data[ySize + i + 1] = ub.get(i);
            }
        }

        return data;
    }

    public static int[] getRGBIntFromPlanes(int mHeight, Image.Plane[] planes){

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
                if (uvPos >= uvCapacity-1)
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
                final int v = (vPlane.get(uvPos+1) & 0xff) - 128;
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

