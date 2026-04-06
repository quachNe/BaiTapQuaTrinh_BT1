package com.example.baitapquatrinh_bt1.model;

import java.util.Map;

public class GoldResponse {

    public Map<String, GoldItem> prices;

    public static class GoldItem {
        public String name;
        public double buy;
        public double sell;
        public double change_buy;
        public String currency;
    }

    // Lấy giá bán theo loại vàng (dùng cho Convert)
    public double getSellPrice(String key) {
        if (prices == null) return 0;

        GoldItem item = prices.get(key);
        if (item != null) {
            return item.sell;
        }

        return 0;
    }

    // Lấy giá mua (nếu cần)
    public double getBuyPrice(String key) {
        if (prices == null) return 0;

        GoldItem item = prices.get(key);
        if (item != null) {
            return item.buy;
        }

        return 0;
    }
}