package ru.krotarnya.diasync;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class Alerter {
    private static Alerter instance;
    private final String TAG = "Alerter";
    AudioManager audio_manager;
    Context context;

    public Alerter(Context context) {
        this.context = context;
        audio_manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio_manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audio_manager.setSpeakerphoneOn(true);
        audio_manager.setStreamVolume(AudioManager.STREAM_MUSIC, audio_manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    public void detonate() {
        Log.d(TAG, "Detonated");
        MediaPlayer player = MediaPlayer.create(context, R.raw.alarm_low);
        player.start();
    }

    public void checkAlerts() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("libre2_low_alert_enabled", false) ||
            prefs.getBoolean("libre2_high_alert_enabled", false) ||
            prefs.getBoolean("libre2_no_data_alert_enabled", false)) {

        }

    }
}
