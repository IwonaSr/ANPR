package com.example.ejwon.anpr.common;

import org.opencv.core.Point;

/**
 * Created by Ejwon on 2017-09-16.
 */
public class Coordinate {

    Point p1;
    Point p2;
    int el;

    public Coordinate(Point p1, Point p2, int el){
        this.p1 = p1;
        this.p2 = p2;
        this.el = el;
    }

    public int getEl() {
        return el;
    }

    public void setEl(int el) {
        this.el = el;
    }

    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }

    public Point getP2() {
        return p2;
    }

    public void setP2(Point p2) {
        this.p2 = p2;
    }
}
