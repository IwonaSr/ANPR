package com.example.ejwon.anpr.common;

/**
 * Created by Ejwon on 2017-09-11.
 */
public class Result {

    private String recognizedNumber;
    private String recognizedTown;
    private Index index;
    private Time allTimes;

    public Result(){

    }

    public Result(String recognizedNumber, String recognizedTown) {
        this.recognizedNumber = recognizedNumber;
        this.recognizedTown = recognizedTown;
    }

    public Result(Time allTimes, String recognizedTown, String recognizedNumber) {
        this.allTimes = allTimes;
        this.recognizedTown = recognizedTown;
        this.recognizedNumber = recognizedNumber;
    }

    public Result(String recognizedNumber, String recognizedTown, Time allTimes, Index index) {
        this.recognizedNumber = recognizedNumber;
        this.recognizedTown = recognizedTown;
        this.allTimes = allTimes;
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Time getAllTimes() {
        return allTimes;
    }

    public void setAllTimes(Time allTimes) {
        this.allTimes = allTimes;
    }

    public String getRecognizedNumber() {
        return recognizedNumber;
    }

    public void setRecognizedNumber(String recognizedNumber) {
        this.recognizedNumber = recognizedNumber;
    }

    public String getRecognizedTown() {
        return recognizedTown;
    }

    public void setRecognizedTown(String recognizedTown) {
        this.recognizedTown = recognizedTown;
    }

    String displayResult(){
        return "(" + this.index.getNumber() + ")" + this.recognizedNumber + ":"
                + this.index.getTown()+ ":" + this.recognizedTown + ":" + this.allTimes.timeToDisplay()  + ",";
    }

    String displayResultWithoutTime(){
        return "(" + this.index.getNumber() + ")" + this.recognizedNumber + ":"
                + this.index.getTown()+ ":" + this.recognizedTown + ", ";
    }

}
