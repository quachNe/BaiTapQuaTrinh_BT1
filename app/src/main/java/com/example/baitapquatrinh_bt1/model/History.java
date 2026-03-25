package com.example.baitapquatrinh_bt1.model;

public class History {
    public int id;
    public String date;
    public String goldType;
    public double amount;
    public String unit;
    public double result;

    public History(int id, String date, String goldType, double amount, String unit, double result) {
        this.id = id;
        this.date = date;
        this.goldType = goldType;
        this.amount = amount;
        this.unit = unit;
        this.result = result;
    }
}