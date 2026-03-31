package com.example.baitapquatrinh_bt1.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.baitapquatrinh_bt1.R;
import com.example.baitapquatrinh_bt1.model.GoldResponse;
import com.example.baitapquatrinh_bt1.network.ApiService;
import com.example.baitapquatrinh_bt1.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.*;

public class WidgetUtils {

    private static final String PREF = "WIDGET_PREF";
    private static final String INDEX = "INDEX";

    // 🔥 CACHE DATA (QUAN TRỌNG)
    private static List<GoldResponse.GoldItem> cacheList = null;

    // ================= UPDATE =================
    public static void updateWidget(Context context) {

        // 👉 Nếu đã có cache → không gọi API nữa
        if (cacheList != null && !cacheList.isEmpty()) {
            updateFromCache(context);
            return;
        }

        // 👉 Chưa có → gọi API
        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.getGoldPrices().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> res) {

                try {
                    if (res.body() == null || res.body().prices == null) {
                        updateTimeOnly(context, "No data");
                        return;
                    }

                    List<GoldResponse.GoldItem> list = new ArrayList<>();

                    for (String key : res.body().prices.keySet()) {
                        GoldResponse.GoldItem item = res.body().prices.get(key);

                        if ("VND".equals(item.currency)) {
                            list.add(item);
                        }
                    }

                    if (list.isEmpty()) {
                        updateTimeOnly(context, "No VND");
                        return;
                    }

                    // sort ổn định
                    Collections.sort(list, (a, b) -> a.name.compareTo(b.name));

                    // 🔥 LƯU CACHE
                    cacheList = list;

                    updateFromCache(context);

                } catch (Exception e) {
                    Log.e("WIDGET", "Parse error", e);
                    updateTimeOnly(context, "Parse error");
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                Log.e("WIDGET", "API error", t);
                updateTimeOnly(context, "Lỗi mạng");
            }
        });
    }

    // ================= UPDATE FROM CACHE =================
    private static void updateFromCache(Context context) {

        if (cacheList == null || cacheList.isEmpty()) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_gold);

        int index = getIndex(context);

        // 🔥 FIX CHÍNH: KHÔNG reset về 0 nữa
        index = index % cacheList.size();
        saveIndex(context, index);

        GoldResponse.GoldItem item = cacheList.get(index);

        Log.d("WIDGET", "Index: " + index + "/" + cacheList.size());
        Log.d("WIDGET", "Name: " + item.name);

        // ===== UI =====
        views.setTextViewText(R.id.txtName, item.name);
        views.setTextViewText(R.id.txtBuy, "Mua:" + format(item.buy));
        views.setTextViewText(R.id.txtSell,"Bán:" + format(item.sell));

        // ===== TIME =====
        views.setTextViewText(R.id.txtTime, getTime());
        views.setTextViewText(R.id.txtDate, getDate());
        views.setTextViewText(R.id.txtUpdated, "Cập nhật: " + getTime());

        setButtons(context, views);

        AppWidgetManager.getInstance(context)
                .updateAppWidget(
                        new ComponentName(context, GoldWidget.class),
                        views
                );
    }

    // ================= FALLBACK =================
    private static void updateTimeOnly(Context context, String msg) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_gold);

        views.setTextViewText(R.id.txtTime, getTime());
        views.setTextViewText(R.id.txtDate, getDate());
        views.setTextViewText(R.id.txtUpdated, msg);

        setButtons(context, views);

        AppWidgetManager.getInstance(context)
                .updateAppWidget(
                        new ComponentName(context, GoldWidget.class),
                        views
                );
    }

    // ================= BUTTON =================
    private static void setButtons(Context context, RemoteViews views) {

        // NEXT
        Intent nextIntent = new Intent(context, GoldWidget.class);
        nextIntent.setAction(GoldWidget.ACTION_NEXT);
        nextIntent.setPackage(context.getPackageName());

        PendingIntent nextPending = PendingIntent.getBroadcast(
                context,
                100,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.btnNext, nextPending);

        // RELOAD
        Intent reloadIntent = new Intent(context, GoldWidget.class);
        reloadIntent.setAction(GoldWidget.ACTION_RELOAD);
        reloadIntent.setPackage(context.getPackageName());

        PendingIntent reloadPending = PendingIntent.getBroadcast(
                context,
                200,
                reloadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.btnReload, reloadPending);
    }

    // ================= NEXT =================
    public static void nextGold(Context context) {

        int index = getIndex(context);
        index++;

        saveIndex(context, index);

        Log.d("WIDGET", "Saved index: " + index);

        // 🔥 KHÔNG gọi API nữa → dùng cache
        updateFromCache(context);
    }

    // ================= RELOAD =================
    public static void reload(Context context) {

        // 🔥 clear cache → gọi lại API
        cacheList = null;

        updateWidget(context);
    }

    // ================= PREF =================
    private static int getIndex(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getInt(INDEX, 0);
    }

    private static void saveIndex(Context context, int index) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putInt(INDEX, index)
                .apply();
    }

    // ================= FORMAT =================
//    private static String format(double price) {
//        return String.format(Locale.getDefault(), "%.1fM", price / 1_000_000);
//    }

    private static  String format(double number) {
        double trieu = number / 1_000_000;
        return String.format("%.1f\ntriệu", trieu);
    }

    private static String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());
    }

    private static String getDate() {
        return new SimpleDateFormat(
                "EEEE, d, 'tháng' M",
                new Locale("vi", "VN")
        ).format(new Date());
    }
}