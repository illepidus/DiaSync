package ru.krotarnya.diasync;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.TypedValue;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;

public class Alerter {
    private static Alerter instance;
    private static final String TAG = "Alerter";
    private final AudioManager manager = (AudioManager)Diasync.getContext().getSystemService(Context.AUDIO_SERVICE);
    private final SharedPreferences prefs;
    private long snoozed_till;

    public Alerter() {
        prefs = PreferenceManager.getDefaultSharedPreferences(Diasync.getContext());
        Log.d(TAG, "Constructor called in context");
        snoozed_till = Long.parseLong(prefs.getString("alarm_snoozed_till", String.valueOf(System.currentTimeMillis())));
    }

    public static synchronized Alerter getInstance() {
        if (instance == null) {
            instance = new Alerter();
        }
        return instance;
    }

    public static void checkAlerts() {
        Log.d(TAG, "Checking alerts status");
        long millis = System.currentTimeMillis();
        if (getInstance().snoozed_till > millis) {
            Log.d(TAG, "Alerts are snoozed. " + ((getInstance().snoozed_till - millis) / 1000) + " seconds left");
            return;
        }

        snooze(millis + 40000);

        boolean low_alert = getInstance().prefs.getBoolean("libre2_low_alert_enabled", false);
        boolean high_alert = getInstance().prefs.getBoolean("libre2_high_alert_enabled", false);
        boolean no_data_alert = getInstance().prefs.getBoolean("libre2_no_data_alert_enabled", false);
        boolean use_calibrations = getInstance().prefs.getBoolean("libre2_widget_use_calibration", true);

        if (low_alert || high_alert || no_data_alert) {
            Log.d(TAG, "Alerts enabled");
            Libre2ValueList libre2_values = DiasyncDB.getInstance(Diasync.getContext()).getLibre2Values(0, Long.MAX_VALUE, 2);
            if (libre2_values.size() > 0) {
                if (low_alert && (!libre2_values.get(0).isLow(use_calibrations)))
                    low_alert = false;
                if (high_alert && (!libre2_values.get(0).isHigh(use_calibrations)))
                    high_alert = false;
                if (no_data_alert && (millis - libre2_values.get(0).timestamp < 300000))
                    no_data_alert = false;
            }
            if (libre2_values.size() == 2) {
                if (low_alert && (libre2_values.get(0).getValue(use_calibrations) > libre2_values.get(1).getValue(use_calibrations)))
                    low_alert = false;
                if (high_alert && (libre2_values.get(0).getValue(use_calibrations) < libre2_values.get(1).getValue(use_calibrations)))
                    high_alert = false;
            }

            if (low_alert) alert(R.raw.alarm_low);
            else if (high_alert) alert(R.raw.alarm_high);
            else if (no_data_alert) alert(R.raw.alarm_no_data);
        }
    }

    private static void alert(int resource) {
        Context context = Diasync.getContext();
        AudioManager manager = getInstance().manager;
        final int stream_type = AudioManager.STREAM_MUSIC;
        final int cur_volume = manager.getStreamVolume(stream_type);
        final int max_volume = manager.getStreamMaxVolume(stream_type);

        try {
            TypedValue value = new TypedValue();
            context.getResources().getValue(resource, value, true);
            Log.d(TAG, "Ding-ding-ding: " + value.string.toString());
            try {
                manager.setStreamVolume(stream_type, max_volume, 0);
                MediaPlayer player = MediaPlayer.create(context, resource);
                player.setLooping(false);
                player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
                player.setOnCompletionListener(mp -> new Thread(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Wasn't able to Thread.sleep");
                    }
                    mp.release();
                    manager.setStreamVolume(stream_type, cur_volume, 0);
                }).start());
                player.start();
            } catch (Exception e) {
                Log.e(TAG, "Wasn't able to play sound: " + e);
                e.printStackTrace();
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found");
        }
    }

    public static void snooze(long till) {
        if (till < getInstance().snoozed_till) {
            Log.d(TAG, "Already snoozed");
            return;
        }
        Log.d(TAG, "Snoozing till " + Diasync.dateTimeFormat(till));
        getInstance().snoozed_till = till;
        SharedPreferences.Editor editor = getInstance().prefs.edit();
        editor.putString("alarm_snoozed_till", String.valueOf(till));
        editor.apply();
    }

    public static void resume() {
        getInstance().snoozed_till = 0;
        SharedPreferences.Editor editor = getInstance().prefs.edit();
        editor.putString("alarm_snoozed_till", String.valueOf(0));
        editor.apply();
    }

    public static boolean isSnoozedExternally() {
        return (getInstance().snoozed_till > System.currentTimeMillis() + 40000);
    }

    public static long snoozedTill() {
        return getInstance().snoozed_till;
    }
}
