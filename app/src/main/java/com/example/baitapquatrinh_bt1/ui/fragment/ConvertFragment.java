package com.example.baitapquatrinh_bt1.ui.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import com.example.baitapquatrinh_bt1.R;
import com.example.baitapquatrinh_bt1.database.DatabaseHelper;
import com.example.baitapquatrinh_bt1.model.GoldResponse;
import com.example.baitapquatrinh_bt1.network.ApiService;
import com.example.baitapquatrinh_bt1.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConvertFragment extends Fragment {

    private Spinner spinnerGoldType;
    private Spinner spinnerUnit;
    private EditText edtAmount;
    private TextView txtResult;
    private ListView listHistory;
    private Button btnClearHistory;

    private GoldResponse goldData;

    private ArrayAdapter<String> adapter;
    private DatabaseHelper db;

    private Map<String, String> nameToKeyMap = new HashMap<>();

    // 👉 lưu result để tránh parse text lỗi
    private double lastResult = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_convert, container, false);

        spinnerGoldType = view.findViewById(R.id.spinnerGoldType);
        spinnerUnit = view.findViewById(R.id.spinnerUnit);
        edtAmount = view.findViewById(R.id.edtAmount);
        txtResult = view.findViewById(R.id.txtResult);
        listHistory = view.findViewById(R.id.listHistory);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);

        db = new DatabaseHelper(getContext());

        setupSpinner();
        loadData();
        setupListener();
        loadHistory();

        // 👉 Nút XÓA LỊCH SỬ
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

        // 👉 ENTER để lưu (KHÔNG dùng isHandled nữa)
        edtAmount.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                calculate();

                String input = edtAmount.getText().toString();
                if (!input.isEmpty()) {

                    double amount = Double.parseDouble(input);
                    String name = spinnerGoldType.getSelectedItem().toString();
                    String unit = spinnerUnit.getSelectedItem().toString();

                    db.insertHistory(name, amount, unit, lastResult);

                    loadHistory();
                }

                // 👉 reset để nhập tiếp
                edtAmount.setText("");
                edtAmount.requestFocus();

                return true;
            }

            return false;
        });

        return view;
    }

    private void setupSpinner() {
        String[] units = {"Lượng", "Chỉ", "Phân"};

        ArrayAdapter<String> adapterUnit = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                units
        );
        spinnerUnit.setAdapter(adapterUnit);
    }

    private void loadData() {
        Log.d("API", "Calling API...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.getGoldPrices().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    goldData = response.body();

                    if (goldData.prices != null) {

                        List<String> nameList = new ArrayList<>();
                        nameToKeyMap.clear();

                        for (String key : goldData.prices.keySet()) {
                            GoldResponse.GoldItem item = goldData.prices.get(key);

                            if (item != null) {
                                String name = item.name;

                                nameList.add(name);
                                nameToKeyMap.put(name, key);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_spinner_dropdown_item,
                                nameList
                        );

                        spinnerGoldType.setAdapter(adapter);
                    }

                } else {
                    Log.d("API", "Response fail");
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                Log.e("API", "ERROR: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListener() {

        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        spinnerGoldType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void calculate() {
        if (goldData == null) return;

        String input = edtAmount.getText().toString();
        if (input.isEmpty()) return;

        double amount = Double.parseDouble(input);

        String name = spinnerGoldType.getSelectedItem().toString();
        String key = nameToKeyMap.get(name);
        if (key == null) return;

        double price = goldData.getSellPrice(key);

        String unit = spinnerUnit.getSelectedItem().toString();

        double multiplier = 1;

        if (unit.equals("Chỉ")) {
            multiplier = 0.1;
        } else if (unit.equals("Phân")) {
            multiplier = 0.01;
        }
        double result = amount * price * multiplier;

        lastResult = result; // 👉 lưu lại để insert

        txtResult.setText(String.format("%,.0f VND", result));
    }

    private void loadHistory() {
        List<String> historyList = db.getHistory();

        adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                historyList
        );

        listHistory.setAdapter(adapter);
    }
}