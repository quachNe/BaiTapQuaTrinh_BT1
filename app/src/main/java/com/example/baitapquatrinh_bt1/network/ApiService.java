package com.example.baitapquatrinh_bt1.network;

import com.example.baitapquatrinh_bt1.model.GoldHistoryResponse;
import com.example.baitapquatrinh_bt1.model.GoldResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    // Lấy danh sách giá vàng (trang home)
    @GET("api/prices")
    Call<GoldResponse> getGoldPrices();
    // Lấy thống kê 7 ngày qua (trang chart)
    @GET("api/prices")
    Call<GoldHistoryResponse> getGoldHistory(
            @Query("type") String type,
            @Query("days") int days
    );
}
