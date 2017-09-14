package com.example.ejwon.anpr.imageConversion;

import android.util.Log;

import com.example.ejwon.anpr.common.ReadWriteImageFile;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class AspectOfPlate {

    public static Mat deskew(Mat src, double angle) {

        Point center = new Point(src.width() / 2, src.height() / 2);
        Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);

        //1.0 means 100 % scale
        Size size = new Size(src.width(), src.height());
        Mat dst = new Mat();
        Imgproc.warpAffine(src, dst, rotImage, size, Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);
        return dst;
    }

    public static Mat computeSkew2(MatOfPoint2f mat2f, Mat charImage) {
        Mat result = null;
        RotatedRect rotatedRect = null;
        if (mat2f.rows() > 0) {
            //Get rotated rect of white pixels
            rotatedRect = Imgproc.minAreaRect(mat2f); // zwraca najmniejszy prostokąt, ktory otacza znak
            Log.d("AspectOfPlayt", "RotatedRec from MinAreaRedt: " + rotatedRect);
        }

        //ponizej funkcja tylko do rysowania zaznaczonych najmniejszych prostokątów
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        List<MatOfPoint> boxContours = new ArrayList<>();
        boxContours.add(new MatOfPoint(vertices));
        Imgproc.drawContours(charImage, boxContours, 0, new Scalar(255, 255, 255), 1); //jesli ostatni parametr -1 to wypelnia kontur, jesli >0 to tylko cienki kontur
        ReadWriteImageFile.saveImageToFile(charImage, "ANPR_drawCountours/"); //zwraca szarą prostokat

        //obliczanie kąta do obrócenia obrazu
        Log.i("Angle", "rotatedRect.angle: " + rotatedRect.angle);
        rotatedRect.angle = rotatedRect.angle < -45 ? rotatedRect.angle + 90.f : rotatedRect.angle;
        Log.i("Angle", "rotatedRect.anglec after: " + rotatedRect.angle);

        result = deskew(charImage, rotatedRect.angle);
        return result;
    }

    public static Mat computeSkew(Mat charImage) {

        //Invert the colors (because objects are represented as white pixels, and the background is represented by black pixels)
        Mat bilateral = new Mat();
//        Core.bitwise_not(charImage, invertedImage);
//        ReadWriteImageFile.saveImageToFile(invertedImage, "/invertedImage");

//        Imgproc.GaussianBlur(charImage, bilateral, new Size(1, 1), 1, 1);
//        ReadWriteImageFile.saveImageToFile(bilateral, "/bilateral");

        Mat cannyImage = new Mat();
        Imgproc.Canny(charImage, cannyImage, 100, 200, 3, false);
        ReadWriteImageFile.saveImageToFile(cannyImage, "/cannyImage");
        Imgproc.dilate(cannyImage, cannyImage, new Mat());
        ReadWriteImageFile.saveImageToFile(cannyImage, "/dilate");


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat(cannyImage.rows(),
                cannyImage.cols(), CvType.CV_8UC1,
                new Scalar(0));

        Imgproc.findContours(cannyImage, contours, hierarchy ,Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
        Mat result = null;

        for (int i = 0; i < contours.size(); i++) {
            List<Point> goodpoints = new ArrayList<Point>();
            Mat contour = contours.get(i);
            int num = (int) contour.total(); //return the number of array elements
            int buff[] = new int[num * 2]; // [x1, y1, x2, y2, ...]
            contour.get(0, 0, buff);
            for (int q = 0; q < num * 2; q = q + 2) {
                goodpoints.add(new Point(buff[q], buff[q + 1])); //P(buff[0], buff[1], P(buff[2], buff[3]),  P(buff[4], buff[5])- ArrayLista punktów
            }
            MatOfPoint points = new MatOfPoint();
            points.fromList(goodpoints);
            Rect boundingRect = Imgproc.boundingRect(points);

            Size sizeCanny = cannyImage.size();
            double w = sizeCanny.width;
            double h = sizeCanny.height;

            if (((boundingRect.height / boundingRect.width) >= 2.8)
                    && ((boundingRect.height / boundingRect.width) <= 4.5))
            {
                MatOfPoint cnt =  contours.get(i);
                MatOfPoint2f mat2f = new MatOfPoint2f(cnt.toArray());

                RotatedRect rotatedRect = null;
                if (mat2f.rows() > 0) {
                    //Get rotated rect of white pixels
                    rotatedRect = Imgproc.minAreaRect(mat2f); // zwraca najmniejszy prostokąt, ktory otacza znak
                    Log.d("AspectOfPlayt","RotatedRec from MinAreaRedt: " + rotatedRect);
                }

                //ponizej funkcja tylko do rysowania zaznaczonych najmniejszych prostokątów
                Point[] vertices = new Point[4];
                rotatedRect.points(vertices);
                List<MatOfPoint> boxContours = new ArrayList<>();
                boxContours.add(new MatOfPoint(vertices));
                Imgproc.drawContours( cannyImage, boxContours, 0, new Scalar(255, 0, 0), 1); //jesli ostatni parametr -1 to wypelnia kontur, jesli >0 to tylko cienki kontur
                ReadWriteImageFile.saveImageToFile(cannyImage, "ANPR_drawCountours/"); //zwraca szarą prostokat

                //obliczanie kąta do obrócenia obrazu
                Log.i("Angle", "rotatedRect.angle: " + rotatedRect.angle);
                rotatedRect.angle = rotatedRect.angle < -45 ? rotatedRect.angle + 90.f : rotatedRect.angle;
                Log.i("Angle", "rotatedRect.anglec after: " + rotatedRect.angle);

                return result = deskew(charImage, rotatedRect.angle );
            }
        }


        return result;
    }

    public static Mat computeSkewByHoughLines(Mat charImage) {

        // na pewno zle oblicza kąt
        Size size = charImage.size();
        Mat invertedCharacter = new Mat();
        Core.bitwise_not(charImage, invertedCharacter);
        Mat lines = new Mat();
        Imgproc.HoughLinesP(invertedCharacter, lines, 1, Math.PI / 180, 100, size.width / 2.f, 20);
        double angle = 0.;
        for (int i = 0; i < lines.height(); i++) {
            for (int j = 0; j < lines.width(); j++) {
                angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
            }
        }
        angle /= lines.size().area();
        angle = angle * 180 / Math.PI;

        Log.i("HoughMethod", "Angle of rotated plate: " + angle);

        Mat result = deskew(charImage, angle);

        return result;
    }

}
