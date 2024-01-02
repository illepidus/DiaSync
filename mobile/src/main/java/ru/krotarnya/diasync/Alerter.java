package ru.krotarnya.diasync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import androidx.preference.PreferenceManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import ru.krotarnya.diasync.model.Libre2ValueList;

public class Alerter {
    private static volatile Alerter instance;
    private static final String TAG = "Alerter";
    private static final Duration SILENCE_INTERVAL = Duration.ofSeconds(55);
    private static final Duration NO_DATA_INTERVAL = Duration.ofMinutes(5);
    private static final Duration CHECK_INTERVAL = Duration.ofMinutes(1);

    private final SharedPreferences prefs;
    private Instant snoozedTill;
    private Instant externalSnoozedTill;

    public Alerter() {
        prefs = PreferenceManager.getDefaultSharedPreferences(Diasync.getContext());
        Log.d(TAG, "Constructor called in context");
        snoozedTill = Instant.ofEpochMilli(Long.parseLong(prefs.getString("alarm_snoozed_till", String.valueOf(System.currentTimeMillis()))));
        externalSnoozedTill = Instant.ofEpochMilli(Long.parseLong(prefs.getString("alarm_external_snoozed_till", String.valueOf(System.currentTimeMillis()))));
        new Timer().schedule(new TimerTask() {
            public void run() {
                check();
            }
        }, CHECK_INTERVAL.toMillis(), CHECK_INTERVAL.toMillis());
    }

    public static Alerter getInstance() {
        Alerter localInstance = instance;
        if (localInstance == null) {
            synchronized (Alerter.class) {
                localInstance = instance;
                if (localInstance == null) {
                    localInstance = new Alerter();
                    instance = localInstance;
                }
            }
        }
        return localInstance;
    }

    public static synchronized void check() {
        Log.d(TAG, "Checking...");
        Instant now = Instant.now();
        if (getInstance().snoozedTill.isAfter(now)) {
            Log.d(TAG, "Alerts are snoozed. "
                    + (getInstance().snoozedTill.getEpochSecond() - now.getEpochSecond())
                    + " seconds left");
            return;
        }
        snooze(now.plus(SILENCE_INTERVAL));

        boolean lowAlert = getInstance().prefs.getBoolean("libre2_low_alert_enabled", false);
        boolean highAlert = getInstance().prefs.getBoolean("libre2_high_alert_enabled", false);
        boolean noDataAlert = getInstance().prefs.getBoolean("libre2_no_data_alert_enabled", false);

        if (lowAlert || highAlert || noDataAlert) {
            Log.d(TAG, "Alerts enabled");
            Libre2ValueList libre2_values = DiasyncDB.getInstance(Diasync.getContext()).getLibre2Values(0, Long.MAX_VALUE, 2);
            if (libre2_values.size() > 0) {
                if (lowAlert && (!libre2_values.get(0).isLow()))
                    lowAlert = false;
                if (highAlert && (!libre2_values.get(0).isHigh()))
                    highAlert = false;
                if (noDataAlert && (Duration.between(Instant.ofEpochMilli(libre2_values.get(0).timestamp).plus(NO_DATA_INTERVAL), now).isNegative()))
                    noDataAlert = false;
            }
            if (libre2_values.size() == 2) {
                if (lowAlert && (libre2_values.get(0).getValue() >= libre2_values.get(1).getValue()))
                    lowAlert = false;
                if (highAlert && (libre2_values.get(0).getValue() <= libre2_values.get(1).getValue()))
                    highAlert = false;
            }

            if (lowAlert) alert(R.raw.alert_low);
            else if (highAlert) alert(R.raw.alert_high);
            else if (noDataAlert) alert(R.raw.alert_no_data);
        }
    }

    private static synchronized void alert(int resource_id) {
        Context context = Diasync.getContext();
        Resources resources = context.getResources();
        alert(new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resource_id))
                .appendPath(resources.getResourceTypeName(resource_id))
                .appendPath(resources.getResourceEntryName(resource_id))
                .build());
    }

    private static synchronized void alert(Uri uri) {
        Context context = Diasync.getContext();
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(context, uri);
        } catch (Exception e) {
            Log.e(TAG, "Exception caught on prepare: " + e);
            return;
        }

        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        mediaPlayer.setOnCompletionListener(mp -> new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(TAG, "Wasn't able to Thread.sleep");
            }
            mp.release();
        }).start());
        mediaPlayer.prepareAsync();
    }

    public static synchronized void externalSnooze(Instant till) {
        getInstance().externalSnoozedTill = till;
        SharedPreferences.Editor editor = getInstance().prefs.edit();
        editor.putString("alarm_external_snoozed_till", String.valueOf(till.toEpochMilli()));
        editor.apply();
        snooze(till);
    }

    public static synchronized void snooze(Instant till) {
        if (till.isBefore(getInstance().snoozedTill)) {
            Log.d(TAG, "Already snoozed");
            return;
        }
        Log.d(TAG, "Snoozing till " + till);
        getInstance().snoozedTill = till;
        SharedPreferences.Editor editor = getInstance().prefs.edit();
        editor.putString("alarm_snoozed_till", String.valueOf(till.toEpochMilli()));
        editor.apply();
    }


    public static synchronized void resume() {
        getInstance().snoozedTill = Instant.EPOCH;
        SharedPreferences.Editor editor = getInstance().prefs.edit();
        editor.putString("alarm_snoozed_till", "0");
        editor.apply();
    }

    public static synchronized boolean isSnoozedExternally() {
        Alerter instance = getInstance();
        return instance.snoozedTill.isAfter(Instant.now())
                && instance.externalSnoozedTill.equals(instance.snoozedTill);
    }

    public static synchronized Instant snoozedTill() {
        return getInstance().snoozedTill;
    }
}
