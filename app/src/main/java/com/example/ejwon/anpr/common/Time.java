package com.example.ejwon.anpr.common;

/**
 * Created by Ejwon on 2017-09-13.
 */
public class Time {

    long preProcessingTime;
    long segmentationTime;
    long ocrTime;
    long sumTime;

    public Time(long preprocessingTime, long segmentationTime, long ocrTime, long sumTime){

    }
    public long getPreProcessingTime() {
        return preProcessingTime;
    }

    public void setPreProcessingTime(long preProcessingTime) {
        this.preProcessingTime = preProcessingTime;
    }

    public long getSegmentationTime() {
        return segmentationTime;
    }

    public void setSegmentationTime(long segmentationTime) {
        this.segmentationTime = segmentationTime;
    }

    public long getOcrTime() {
        return ocrTime;
    }

    public void setOcrTime(long ocrTime) {
        this.ocrTime = ocrTime;
    }

    public long getSumTime() {
        return sumTime;
    }

    public void setSumTime(long sumTime) {
        this.sumTime = sumTime;
    }

    public String timeToDisplay(){

        return "[" + this.preProcessingTime + "," + this.getSegmentationTime() + "," + this.getOcrTime() + "," + this.sumTime + "]";
    }

}
