package ru.krotarnya.diasync;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Diasync extends Application {
    private static Diasync instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static void clearDataForceClose() {
        ((ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE))
                .clearApplicationUserData();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getContext().startActivity(homeIntent);
    }

    public static String timeFormat(Instant timestamp) {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(timestamp.toEpochMilli());
    }

    @SuppressLint("DefaultLocale")
    public static String durationFormat(Duration duration) {
        long hr = TimeUnit.MILLISECONDS.toHours(duration.toMillis());
        long min = TimeUnit.MILLISECONDS.toMinutes(duration.toMillis()) % 60;
        long sec = TimeUnit.MILLISECONDS.toSeconds(duration.toMillis()) % 60;
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }
}
