package com.example.baitapquatrinh_bt1.model;

import java.util.List;
import java.util.Map;

public class GoldHistoryResponse {
    public boolean success;
    public int days;
    public String type;
    public List<HistoryItem> history;

    public static class HistoryItem {
        public String date;
        public Map<String, GoldPrice> prices;
    }

    public static class GoldPrice {
        public String name;
        public double buy;
        public double sell;
    }
}
