package com.uttam.callrecord.backuppro.model;

public class UserInfoModelClass {
    private String email;
    private String topics;
    private String parentReferCode;
    private String myReferCode;
    private String paidStatus;
    private String payTime;
    private String expireTime;
    private String myBalance;
    private String myReferCount;

    public UserInfoModelClass() {
    }

    public UserInfoModelClass(String email, String topics, String parentReferCode, String myReferCode, String paidStatus, String payTime, String expireTime, String myBalance, String myReferCount) {
        this.email = email;
        this.topics = topics;
        this.parentReferCode = parentReferCode;
        this.myReferCode = myReferCode;
        this.paidStatus = paidStatus;
        this.payTime = payTime;
        this.expireTime = expireTime;
        this.myBalance = myBalance;
        this.myReferCount = myReferCount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public String getParentReferCode() {
        return parentReferCode;
    }

    public void setParentReferCode(String parentReferCode) {
        this.parentReferCode = parentReferCode;
    }

    public String getMyReferCode() {
        return myReferCode;
    }

    public void setMyReferCode(String myReferCode) {
        this.myReferCode = myReferCode;
    }

    public String getPaidStatus() {
        return paidStatus;
    }

    public void setPaidStatus(String paidStatus) {
        this.paidStatus = paidStatus;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public String getMyBalance() {
        return myBalance;
    }

    public void setMyBalance(String myBalance) {
        this.myBalance = myBalance;
    }

    public String getMyReferCount() {
        return myReferCount;
    }

    public void setMyReferCount(String myReferCount) {
        this.myReferCount = myReferCount;
    }
}
