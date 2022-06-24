package com.krotarnya.diasync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DiaBroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG = "DiaBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.getAction());
    }
}
