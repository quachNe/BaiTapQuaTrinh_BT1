package com.example.baitapquatrinh_bt1;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GoldApiService {
    @GET("api/prices")
    Call<GoldResponse> getGoldPrices();
}
