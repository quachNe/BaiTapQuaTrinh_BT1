package com.example.baitapquatrinh_bt1.chart;

import android.content.Context;
import android.widget.TextView;

import com.example.baitapquatrinh_bt1.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

public class CustomMarker extends MarkerView {
    private TextView txtContent;

    public CustomMarker(Context context) {
        super(context, R.layout.marker_view);
        txtContent = findViewById(R.id.txtMarker);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        txtContent.setText(String.format("Giá: %.1fM", e.getY() / 1_000_000));
        super.refreshContent(e, highlight);
    }
}
