package com.example.baitapquatrinh_bt1.ui.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.baitapquatrinh_bt1.R;
import com.example.baitapquatrinh_bt1.model.History;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private List<History> list;
    private Context context;

    public HistoryAdapter(Context context, List<History> list) {
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        TextView txtIndex, txtTime, txtName, txtUnit, txtResult;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return list.get(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.item_history, parent, false);

            holder = new ViewHolder();
            holder.txtIndex = view.findViewById(R.id.txtIndex);
            holder.txtTime = view.findViewById(R.id.txtTime);
            holder.txtName = view.findViewById(R.id.txtName);
            holder.txtUnit = view.findViewById(R.id.txtUnit);
            holder.txtResult = view.findViewById(R.id.txtResult);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        History item = list.get(i);

        holder.txtIndex.setText(String.valueOf(i + 1));
        holder.txtTime.setText(item.date);
        holder.txtName.setText(item.goldType);
        holder.txtUnit.setText(item.unit);
        holder.txtResult.setText(
                String.format(new java.util.Locale("vi", "VN"), "%,.0f VND", item.result)
        );

        // highlight dòng mới nhất
        if (i == 0) {
            view.setBackgroundColor(0xFFE8F5E9);
        } else {
            view.setBackgroundColor(0xFFFFFFFF);
        }

        return view;
    }
}