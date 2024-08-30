package ru.krotarnya.diasync;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import ru.krotarnya.diasync.service.WearUpdateService;
import ru.krotarnya.diasync.service.WebUpdateService;
import ru.krotarnya.diasync.widget.WidgetUpdateService;

public class Diasync extends Application {
    private static Diasync instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        startService(new Intent(getApplicationContext(), WebUpdateService.class));
        startService(new Intent(getApplicationContext(), WidgetUpdateService.class));
        startService(new Intent(getApplicationContext(), WearUpdateService.class));
    }

    @SuppressLint("DefaultLocale")
    public static String durationFormat(Duration duration) {
        long hr = TimeUnit.MILLISECONDS.toHours(duration.toMillis());
        long min = TimeUnit.MILLISECONDS.toMinutes(duration.toMillis()) % 60;
        long sec = TimeUnit.MILLISECONDS.toSeconds(duration.toMillis()) % 60;
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }

    public static class Intents {
        public static final String NEW_DATA_AVAILABLE = "ru.krotarnya.diasync.NEW_DATA_AVAILABLE";
    }
}
