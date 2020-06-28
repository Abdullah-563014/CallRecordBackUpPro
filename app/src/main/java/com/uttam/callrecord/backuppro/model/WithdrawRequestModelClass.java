package com.uttam.callrecord.backuppro.model;

public class WithdrawRequestModelClass {

    private String email;
    private String name;
    private String withdrawRequestAmount;
    private String paymentMethod;
    private String paymentMethodInfo;

    public WithdrawRequestModelClass() {
    }

    public WithdrawRequestModelClass(String email, String name, String withdrawRequestAmount, String paymentMethod, String paymentMethodInfo) {
        this.email = email;
        this.name = name;
        this.withdrawRequestAmount = withdrawRequestAmount;
        this.paymentMethod = paymentMethod;
        this.paymentMethodInfo = paymentMethodInfo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWithdrawRequestAmount() {
        return withdrawRequestAmount;
    }

    public void setWithdrawRequestAmount(String withdrawRequestAmount) {
        this.withdrawRequestAmount = withdrawRequestAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethodInfo() {
        return paymentMethodInfo;
    }

    public void setPaymentMethodInfo(String paymentMethodInfo) {
        this.paymentMethodInfo = paymentMethodInfo;
    }
}
