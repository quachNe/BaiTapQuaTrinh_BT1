package com.example.baitapquatrinh_bt1.ui.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.baitapquatrinh_bt1.R;
import com.example.baitapquatrinh_bt1.database.DatabaseHelper;
import com.example.baitapquatrinh_bt1.model.GoldResponse;
import com.example.baitapquatrinh_bt1.model.History;
import com.example.baitapquatrinh_bt1.network.ApiService;
import com.example.baitapquatrinh_bt1.network.RetrofitClient;
import com.example.baitapquatrinh_bt1.ui.adapter.HistoryAdapter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConvertFragment extends Fragment {

    private Spinner spinnerGoldType, spinnerUnit;
    private EditText edtAmount, edtPrice;
    private TextView txtResult;
    private ListView listHistory;
    private Button btnClearHistory, btnConvert, btnReset;

    private GoldResponse goldData;
    private DatabaseHelper db;

    private Map<String, String> nameToKeyMap = new HashMap<>();

    private double lastResult = 0;

    private DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    private DecimalFormat formatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_convert, container, false);

        // Ánh xạ view
        spinnerGoldType = view.findViewById(R.id.spinnerGoldType);
        spinnerUnit = view.findViewById(R.id.spinnerUnit);
        edtAmount = view.findViewById(R.id.edtAmount);
        edtPrice = view.findViewById(R.id.edtPrice);
        txtResult = view.findViewById(R.id.txtResult);
        listHistory = view.findViewById(R.id.listHistory);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        btnConvert = view.findViewById(R.id.btnConvert);
        btnReset = view.findViewById(R.id.btnReset);

        edtPrice.setEnabled(false);

        db = new DatabaseHelper(getContext());

        // Setup DecimalFormat
        symbols.setGroupingSeparator('.');
        formatter = new DecimalFormat("#,###", symbols);

        setupSpinner();
        loadData();
        setupListener();
        loadHistory();

        // Xóa lịch sử
        btnClearHistory.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Xóa lịch sử")
                    .setMessage("Bạn có chắc muốn xóa toàn bộ lịch sử?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        db.clearHistory();
                        loadHistory();
                        Toast.makeText(getContext(), "Đã xóa lịch sử", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Chuyển đổi
        btnConvert.setOnClickListener(v -> {
            String input = edtAmount.getText().toString();
            if (input.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                edtAmount.requestFocus();
                return;
            }

            // Gọi hàm tính toán
            calculate();

            double amount = Double.parseDouble(input);
            String name = spinnerGoldType.getSelectedItem().toString();
            String unit = spinnerUnit.getSelectedItem().toString();

            db.insertHistory(name, amount, unit, lastResult);

            loadHistory();

            Toast.makeText(getContext(), "Đã chuyển đổi & lưu", Toast.LENGTH_SHORT).show();
        });

        // Reset
        btnReset.setOnClickListener(v -> {
            edtAmount.setText("");
            txtResult.setText("0 VND");
            edtAmount.requestFocus();
            spinnerGoldType.setSelection(0);
            spinnerUnit.setSelection(0);
        });

        listHistory.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        return view;
    }

    // ========================= SPINNER =========================
    private void setupSpinner() {
        String[] units = {"Lượng", "Chỉ", "Phân"};

        ArrayAdapter<String> adapterUnit = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner,
                units
        );

        adapterUnit.setDropDownViewResource(R.layout.item_spinner);
        spinnerUnit.setAdapter(adapterUnit);
    }

    // ========================= LOAD API =========================
    private void loadData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.getGoldPrices().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    goldData = response.body();

                    List<String> nameList = new ArrayList<>();
                    nameToKeyMap.clear();

                    for (String key : goldData.prices.keySet()) {
                        GoldResponse.GoldItem item = goldData.prices.get(key);
                        if (!item.currency.equals("VND")) continue;
                        if (item != null) {
                            String name = item.name;
                            nameList.add(name);
                            nameToKeyMap.put(name, key);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            R.layout.item_spinner,
                            nameList
                    );

                    adapter.setDropDownViewResource(R.layout.item_spinner);
                    spinnerGoldType.setAdapter(adapter);

                    updatePrice();
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========================= LISTENER =========================
    private void setupListener() {
        spinnerGoldType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePrice();
                calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePrice();
                calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ========================= UPDATE PRICE =========================
    private void updatePrice() {
        if (goldData == null) return;

        String name = spinnerGoldType.getSelectedItem().toString();
        String key = nameToKeyMap.get(name);
        if (key == null) return;

        double priceLuong = goldData.getSellPrice(key);
        String unit = spinnerUnit.getSelectedItem().toString();
        double finalPrice = priceLuong;

        if (unit.equals("Chỉ")) {
            finalPrice = priceLuong / 10;
        } else if (unit.equals("Phân")) {
            finalPrice = priceLuong / 100;
        }

        edtPrice.setText(formatter.format(finalPrice));
    }

    // ========================= CALCULATE =========================
    private void calculate() {
        String amountStr = edtAmount.getText().toString();
        String priceStr = edtPrice.getText().toString().replace(".", "");

        if (amountStr.isEmpty() || priceStr.isEmpty()) return;

        double amount = Double.parseDouble(amountStr);
        double price = Double.parseDouble(priceStr);
        double result = amount * price;

        lastResult = result;

        txtResult.setText(formatter.format(result) + " VND");
    }

    // ========================= HISTORY =========================
    private void loadHistory() {
        List<History> historyList = db.getHistory();
        HistoryAdapter adapter = new HistoryAdapter(getContext(), historyList);
        listHistory.setAdapter(adapter);
    }
}