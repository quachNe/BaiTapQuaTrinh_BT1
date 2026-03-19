package com.example.baitapquatrinh_bt1.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.baitapquatrinh_bt1.chart.CustomMarker;
import com.example.baitapquatrinh_bt1.network.ApiService;
import com.example.baitapquatrinh_bt1.model.GoldResponse;
import com.example.baitapquatrinh_bt1.model.GoldHistoryResponse;
import com.example.baitapquatrinh_bt1.R;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.*;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChartFragment extends Fragment {

    private LineChart lineChart;
    private Spinner spinner;

    private List<String> nameList = new ArrayList<>();
    private List<String> codeList = new ArrayList<>();

    private String currentType = "SJL1L10";

    public ChartFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        lineChart = view.findViewById(R.id.lineChart);
        spinner = view.findViewById(R.id.spinnerFilter);

        loadSpinner();

        return view;
    }

    // ================= SPINNER =================
    private void loadSpinner() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://giavang.now/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        api.getGoldPrices().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    nameList.clear();
                    codeList.clear();

                    for (String key : response.body().prices.keySet()) {
                        GoldResponse.GoldItem item = response.body().prices.get(key);
                        if (!item.currency.equals("VND")) continue;
                        nameList.add(item.name);
                        codeList.add(key);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            nameList
                    );

                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            currentType = codeList.get(position);
                            loadChart(currentType);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // ================= LOAD CHART =================
    private void loadChart(String type) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://giavang.now/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        api.getGoldHistory(type, 7).enqueue(new Callback<GoldHistoryResponse>() {
            @Override
            public void onResponse(Call<GoldHistoryResponse> call, Response<GoldHistoryResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    drawChart(response.body().history);
                }
            }

            @Override
            public void onFailure(Call<GoldHistoryResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // ================= DRAW CHART =================
    private void drawChart(List<GoldHistoryResponse.HistoryItem> history) {

        ArrayList<Entry> buyEntries = new ArrayList<>();
        ArrayList<Entry> sellEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        Collections.reverse(history);

        for (int i = 0; i < history.size(); i++) {
            GoldHistoryResponse.HistoryItem item = history.get(i);

            GoldHistoryResponse.GoldPrice price = item.prices.get(currentType);

            if (price != null) {
                buyEntries.add(new Entry(i, (float) price.buy));
                sellEntries.add(new Entry(i, (float) price.sell));
                dates.add(item.date.substring(5));
            }
        }

        // ===== DATASET =====
        LineDataSet buySet = new LineDataSet(buyEntries, "Giá mua vào");
        buySet.setColor(Color.BLUE);
        buySet.setCircleRadius(5f);
        buySet.setLineWidth(3f);
        buySet.setValueTextSize(10f);

        LineDataSet sellSet = new LineDataSet(sellEntries, "Giá bán ra");
        sellSet.setColor(Color.RED);
        sellSet.setCircleRadius(5f);
        sellSet.setLineWidth(3f);
        sellSet.setValueTextSize(10f);

        // format số
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.format("%.1fM", entry.getY() / 1_000_000);
            }
        };

        buySet.setValueFormatter(formatter);
        sellSet.setValueFormatter(formatter);

        LineData data = new LineData(buySet, sellSet);
        lineChart.setData(data);

        // ===== TRỤC X =====
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);

        // bật grid dọc
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setGridLineWidth(0.5f);

        // ===== TRỤC Y =====
        lineChart.getAxisLeft().setTextSize(12f);

        // bật grid ngang
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(Color.LTGRAY);
        lineChart.getAxisLeft().setGridLineWidth(0.5f);

        lineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fM", value / 1_000_000);
            }
        });

        lineChart.getAxisRight().setEnabled(false);

        // ===== LEGEND =====
        lineChart.getLegend().setTextSize(13f);
        lineChart.getLegend().setYOffset(20f); // 👈 margin top
        lineChart.getLegend().setXEntrySpace(20f);
        // ===== TẮT DESCRIPTION (vì đã có TextView) =====
        lineChart.getDescription().setEnabled(false);

        // ===== STYLE =====
        lineChart.setBackgroundColor(Color.WHITE);

        // padding cho đẹp
        lineChart.setExtraOffsets(10f, 10f, 10f, 20f);

        lineChart.animateX(1000);
        lineChart.invalidate();

        CustomMarker marker = new CustomMarker(getContext());
        marker.setChartView(lineChart);
        lineChart.setMarker(marker);
    }
}