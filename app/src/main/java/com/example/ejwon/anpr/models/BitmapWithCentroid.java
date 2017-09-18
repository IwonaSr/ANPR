package com.example.ejwon.anpr.models;

import android.graphics.Bitmap;

import org.opencv.core.Point;

public class BitmapWithCentroid implements Comparable<BitmapWithCentroid>{

    public BitmapWithCentroid(Bitmap bitmap, Point centroid, Point pTopLeft, Point pBottomRight) {
        super();
        this.bitmap = bitmap;
        this.centroid = centroid;
        this.pTopLeft = pTopLeft;
        this.pBottomRight = pBottomRight;
    }

    Point centroid;
    Point pTopLeft;
    Point pBottomRight;
    Bitmap bitmap;

    public Point getCentroid() {
        return centroid;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public Point getpTopLeft() {
        return pTopLeft;
    }

    public void setpTopLeft(Point pTopLeft) {
        this.pTopLeft = pTopLeft;
    }

    public Point getpBottomRight() {
        return pBottomRight;
    }

    public void setpBottomRight(Point pBottomRight) {
        this.pBottomRight = pBottomRight;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int compareTo(BitmapWithCentroid another) {
        if((Math.abs(this.centroid.y - another.centroid.y) <= 30)) //inline
            return (int)(this.centroid.x - another.centroid.x);
        else
            return (int)(this.centroid.y - another.centroid.y);
    }

//    public static Comparator<BitmapWithCentroid> compare2Centroids() {
//        return new Comparator<BitmapWithCentroid>() {
//            @Override
//            public int compare(BitmapWithCentroid lhs, BitmapWithCentroid rhs) {
//                if ((Math.abs(lhs.centroid.y - rhs.centroid.y) <= 30)) //inline
//                    return (int) (lhs.centroid.x - rhs.centroid.x);
//                else
//                    return (int) (lhs.centroid.y - rhs.centroid.y);
//            }
//            // compare using attribute 2
//        };
//    }

//    public static Comparator<BitmapWithCentroid> compare2CornerPoints() {
//        return new Comparator<BitmapWithCentroid>() {
//            @Override
//            public int compare(BitmapWithCentroid lhs, BitmapWithCentroid rhs) {
//                int diff = (int )Math.abs(lhs.pBottomRight.x - rhs.pTopLeft.x);
//                if ((diff >= 25 && diff <= 45)) //inline
//                    lhs.get
//                    return diff;
//                else{
//                    return
//                }
//            }
//            // compare using attribute 1
//        };
//    }


    @Override
    public String toString() {
        return "Toa do: " + this.centroid.x + "x" + this.centroid.y;
    }


}
