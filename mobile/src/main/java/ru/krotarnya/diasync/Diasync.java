package ru.krotarnya.diasync;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
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
