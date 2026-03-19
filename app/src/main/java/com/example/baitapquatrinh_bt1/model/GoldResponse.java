package com.example.baitapquatrinh_bt1.model;

import java.util.Map;

public class GoldResponse {
    public Map<String, GoldItem> prices;

    public class GoldItem {
        public String name;
        public double buy;
        public double sell;
        public double change_buy;
        public String currency;
    }

}
