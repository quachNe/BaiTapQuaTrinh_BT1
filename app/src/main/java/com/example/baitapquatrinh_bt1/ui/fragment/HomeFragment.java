package com.example.baitapquatrinh_bt1.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.baitapquatrinh_bt1.network.ApiService;
import com.example.baitapquatrinh_bt1.model.Gold;
import com.example.baitapquatrinh_bt1.network.RetrofitClient;
import com.example.baitapquatrinh_bt1.ui.adapter.GoldAdapter;
import com.example.baitapquatrinh_bt1.model.GoldResponse;
import com.example.baitapquatrinh_bt1.R;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    ListView listGold;
    Spinner spinnerFilter;
    TextView txtDate;

    ArrayList<Gold> goldList;        // hiển thị
    ArrayList<Gold> originalList;    // dữ liệu gốc
    ArrayList<String> filterList;

    GoldAdapter adapter;
    ArrayAdapter<String> spinnerAdapter;
    LinearLayout loadingLayout;
    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ view
        listGold = view.findViewById(R.id.listGold);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        txtDate = view.findViewById(R.id.txtDate);
        loadingLayout = view.findViewById(R.id.loadingLayout);

        // Khởi tạo list
        goldList = new ArrayList<>();
        originalList = new ArrayList<>();
        filterList = new ArrayList<>();

        // Adapter list
        adapter = new GoldAdapter(getContext(), goldList);
        listGold.setAdapter(adapter);

        // Hiển thị ngày hiện tại
        setCurrentDate();

        // Spinner
        spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner,
                filterList
        );

        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = filterList.get(position);
                filterGold(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Gọi API
        fetchGoldPrice();

        return view;
    }

    // ================= DATE =================
    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEEE, 'ngày' d, 'tháng' M, 'năm' yyyy",
                new Locale("vi", "VN")
        );

        String currentDate = sdf.format(new Date());
        txtDate.setText(currentDate);
    }

    // ================= API =================
    private void fetchGoldPrice() {
        loadingLayout.setVisibility(View.VISIBLE);
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://www.vang.today/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        ApiService api = retrofit.create(ApiService.class);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getGoldPrices().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    goldList.clear();
                    originalList.clear();
                    filterList.clear();

                    filterList.add("Tất cả");

                    Map<String, GoldResponse.GoldItem> map = response.body().prices;

                    for (String key : map.keySet()) {

                        GoldResponse.GoldItem item = map.get(key);

                        // Bỏ vàng thế giới
                        if (!item.currency.equals("VND")) continue;

                        Gold gold = new Gold(
                                item.name,
                                formatTrieu(item.buy),
                                formatTrieu(item.sell),
                                getAdvice(item.change_buy)
                        );

                        goldList.add(gold);
                        originalList.add(gold);

                        // thêm vào spinner
                        if (!filterList.contains(item.name)) {
                            filterList.add(item.name);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    spinnerAdapter.notifyDataSetChanged();
                }
                loadingLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                t.printStackTrace();
                loadingLayout.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= FILTER =================
    private void filterGold(String keyword) {

        goldList.clear();

        if (keyword.equals("Tất cả")) {
            goldList.addAll(originalList);
        } else {
            for (Gold g : originalList) {
                if (g.getName().equals(keyword)) {
                    goldList.add(g);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ================= FORMAT =================
    private String formatTrieu(double number) {
        double trieu = number / 1_000_000;
        return String.format("%.1f\ntriệu", trieu);
    }

    private String getAdvice(double change) {
        if (change > 0) return "Nên bán";
        if (change < 0) return "Nên mua";
        return "Nên giữ";
    }
}