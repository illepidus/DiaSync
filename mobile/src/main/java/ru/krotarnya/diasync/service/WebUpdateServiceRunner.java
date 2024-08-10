package ru.krotarnya.diasync.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WebUpdateServiceRunner extends BroadcastReceiver {
    @Override
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, WebUpdateService.class));
    }
}
