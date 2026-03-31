package com.example.baitapquatrinh_bt1.widget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class GoldWorker extends Worker {

    public GoldWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        WidgetUtils.updateWidget(getApplicationContext());
        return Result.success();
    }
}