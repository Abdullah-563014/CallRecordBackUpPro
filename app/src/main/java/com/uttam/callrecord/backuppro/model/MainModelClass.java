package com.uttam.callrecord.backuppro.model;

import java.util.ArrayList;

public class MainModelClass {

    private String date;
    private String month;
    private String year;
    private ArrayList<CallListModelClass> arrayList;

    public MainModelClass(String date, String month, String year, ArrayList<CallListModelClass> arrayList) {
        this.date = date;
        this.month = month;
        this.year = year;
        this.arrayList = arrayList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public ArrayList<CallListModelClass> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<CallListModelClass> arrayList) {
        this.arrayList = arrayList;
    }
}
