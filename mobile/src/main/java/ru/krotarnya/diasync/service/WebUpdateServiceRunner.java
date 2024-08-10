package ru.krotarnya.diasync.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WebUpdateServiceRunner extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent ignored) {
        Intent intent = new Intent(context, WebUpdateService.class);
        context.startForegroundService(intent);
    }
}
