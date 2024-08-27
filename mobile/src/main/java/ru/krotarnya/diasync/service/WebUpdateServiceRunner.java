package ru.krotarnya.diasync.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Optional;

public class WebUpdateServiceRunner extends BroadcastReceiver {
    private static final String TAG = WebUpdateServiceRunner.class.getSimpleName();

    @Override
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received "
                + Optional.ofNullable(intent.getAction()).orElse("unknown")
                + " action intent");
        context.startForegroundService(new Intent(context, WebUpdateService.class));
    }
}
