package com.example.ejwon.anpr.common;

/**
 * Created by Ejwon on 2017-09-13.
 */
public class Index {

    int number;
    int town;

    public Index() {
        this.number = number;
        this.town = town;
    }

    public Index(int number, int town) {
        this.number = number;
        this.town = town;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    public void addNumber() {
        this.number = this.number + 1;
    }
    public void addTown() {
        this.town = this.town + 1;
    }




    public int getTown() {
        return town;
    }

    public void setTown(int town) {
        this.town = town;
    }
}
