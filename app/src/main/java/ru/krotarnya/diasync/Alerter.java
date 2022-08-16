package ru.krotarnya.diasync;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import androidx.preference.PreferenceManager;

public class Alerter {
    private static Alerter instance;
    private static final String TAG = "Alerter";
    private static volatile MediaPlayer player;
    private final Context context = Diasync.getContext();
    private final AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    private final SharedPreferences prefs;
    private long snoozed_till;

    public Alerter() {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "Constructor called in context = " + context);
        snoozed_till = Long.parseLong(prefs.getString("alarm_snoozed_till", String.valueOf(System.currentTimeMillis())));
    }

    public static synchronized Alerter getInstance() {
        if (instance == null) {
            instance = new Alerter();
        }
        return instance;
    }

    public static void checkAlerts() {
        long millis = System.currentTimeMillis();
        if (getInstance().snoozed_till > millis) return;
        getInstance().snoozed_till = millis + 5000;

        boolean low_alert = getInstance().prefs.getBoolean("libre2_low_alert_enabled", false);
        boolean high_alert = getInstance().prefs.getBoolean("libre2_high_alert_enabled", false);
        boolean no_data_alert = getInstance().prefs.getBoolean("libre2_no_data_alert_enabled", false);
        boolean use_calibrations = getInstance().prefs.getBoolean("libre2_widget_use_calibration", true);

        if (low_alert || high_alert || no_data_alert) {
            Log.d(TAG, "Alerts enabled");
            Libre2ValueList libre2_values = DiasyncDB.getInstance(getInstance().context).getLibre2Values(0, Long.MAX_VALUE, 2);
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
        //alert(R.raw.alarm_high); //TODO: remove this
    }

    private static void alert(int resource) {
        Context context = getInstance().context;
        AudioManager manager = getInstance().manager;

        final int stream_type = AudioManager.STREAM_ALARM;
        final int max_volume = manager.getStreamMaxVolume(stream_type);

        try {
            TypedValue value = new TypedValue();
            context.getResources().getValue(resource, value, true);
            Log.d(TAG, "Ding-ding-ding: " + value.string.toString());
            try {
                player = MediaPlayer.create(context, resource);
                player.setLooping(false);
                player.setOnCompletionListener(Alerter::delayedMediaPlayerRelease);
                player.start();
            } catch (Exception e) {
                Log.e(TAG, "Wasn't able to play sound");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found");
            return;
        }
    }

    public static void delayedMediaPlayerRelease(final MediaPlayer mp) {
        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {}
            mp.release();
        }).start();
    }
}
