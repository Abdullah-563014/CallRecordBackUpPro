package com.uttam.callrecord.backuppro.model;

public class DatabaseModelClass {
    private String id;
    private String date;
    private String month;
    private String year;
    private String callIndicator;
    private String duration;
    private String name;
    private String time;
    private String file;
    private String uploadStatus;

    public DatabaseModelClass(String id, String date, String month, String year, String callIndicator, String duration, String name, String time, String file, String uploadStatus) {
        this.id = id;
        this.date = date;
        this.month = month;
        this.year = year;
        this.callIndicator = callIndicator;
        this.duration = duration;
        this.name = name;
        this.time = time;
        this.file = file;
        this.uploadStatus = uploadStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCallIndicator() {
        return callIndicator;
    }

    public void setCallIndicator(String callIndicator) {
        this.callIndicator = callIndicator;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
