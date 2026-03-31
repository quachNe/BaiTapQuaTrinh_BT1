package com.example.baitapquatrinh_bt1.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GoldWidget extends AppWidgetProvider {

    public static final String ACTION_NEXT = "com.example.ACTION_NEXT";
    public static final String ACTION_RELOAD = "com.example.ACTION_RELOAD";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetUtils.updateWidget(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        WidgetUtils.updateWidget(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent == null || intent.getAction() == null) return;

        Log.d("WIDGET", "Action: " + intent.getAction());

        switch (intent.getAction()) {
            case ACTION_NEXT:
                WidgetUtils.nextGold(context);
                break;

            case ACTION_RELOAD:
                WidgetUtils.updateWidget(context);
                break;
        }
    }
}