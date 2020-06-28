package com.uttam.callrecord.backuppro.model;

public class AdminNoticeModelClass {
    private String imageUrl;
    private String targetUrl;

    public AdminNoticeModelClass() {
    }

    public AdminNoticeModelClass(String imageUrl, String targetUrl) {
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
