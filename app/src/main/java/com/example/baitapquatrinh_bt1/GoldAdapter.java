package com.example.baitapquatrinh_bt1;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GoldAdapter extends BaseAdapter {

    Context context;
    List<Gold> list;

    public GoldAdapter(Context context, List<Gold> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int i) { return list.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.item_gold, parent, false);
        }

        TextView txtName = view.findViewById(R.id.txtType);
        TextView txtBuy = view.findViewById(R.id.txtBuy);
        TextView txtSell = view.findViewById(R.id.txtSell);
        TextView txtNote = view.findViewById(R.id.txtNote);

        Gold g = list.get(i);

        txtName.setText(g.getName());
        txtBuy.setText(g.getBuy());
        txtSell.setText(g.getSell());
        txtNote.setText(g.getNote());
        if (g.getNote().equals("Nên mua")) {
            txtNote.setTextColor(Color.GREEN);
        } else if (g.getNote().equals("Nên bán")) {
            txtNote.setTextColor(Color.RED);
        } else {
            txtNote.setTextColor(Color.GRAY);
        }
        return view;
    }
}