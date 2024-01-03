package ru.krotarnya.diasync.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class MainListenerService extends WearableListenerService  {
    private static final String TAG = "MainListenerService";

    public MainListenerService() {
        Log.d(TAG, "constructor call");
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.wtf(TAG, "message received");
    }

    @Override
    public void onPeerConnected(@NonNull Node peer) {
        Log.wtf(TAG, "peer connected");
    }
}
