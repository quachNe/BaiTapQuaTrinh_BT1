package com.example.baitapquatrinh_bt1.model;

public class Gold {
    String name, buy, sell, note;

    public Gold(String name, String buy, String sell, String note) {
        this.name = name;
        this.buy = buy;
        this.sell = sell;
        this.note = note;
    }

    public String getName() { return name; }
    public String getBuy() { return buy; }
    public String getSell() { return sell; }
    public String getNote() { return note; }
}
