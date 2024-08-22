package ru.krotarnya.diasync.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class WebUpdateServiceRunner extends BroadcastReceiver {
    @Override
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("webhook_enabled", false)) {
            context.startForegroundService(new Intent(context, WebUpdateService.class));
        }
    }
}
