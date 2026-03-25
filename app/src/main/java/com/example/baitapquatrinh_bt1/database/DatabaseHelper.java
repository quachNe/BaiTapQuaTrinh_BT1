package com.example.baitapquatrinh_bt1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.baitapquatrinh_bt1.model.History;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên DB + version
    private static final String DB_NAME = "history.db";
    private static final int DB_VERSION = 1;

    // Tên bảng
    private static final String TABLE_NAME = "price_history";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Tạo bảng
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "gold_type TEXT, " +
                "amount REAL, " +
                "unit TEXT, " +
                "result REAL, " +
                "created_at TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nếu update DB thì xoá bảng cũ tạo lại (đơn giản cho bài tập)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // =========================
    // INSERT DATA
    // =========================
    public void insertHistory(String goldType, double amount, String unit, double result) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Lấy ngày hiện tại
        String date = new SimpleDateFormat("dd/MM", Locale.getDefault())
                .format(new Date());

        ContentValues values = new ContentValues();
        values.put("gold_type", goldType);
        values.put("amount", amount);
        values.put("unit", unit);
        values.put("result", result);
        values.put("created_at", date);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // =========================
    // GET DATA
    // =========================
    public List<History> getHistory() {

        List<History> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String goldType = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String unit = cursor.getString(3);
                double result = cursor.getDouble(4);
                String date = cursor.getString(5);

                list.add(new History(id, date, goldType, amount, unit, result));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    // =========================
    // DELETE ALL (bonus)
    // =========================
    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }
}