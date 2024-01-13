package ru.krotarnya.diasync.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    @Override
    public void onCreate() {
        Log.d(TAG, "Service created");
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(result -> Log.d(TAG, "Token = " + result.getResult()));
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.d(TAG, "From: " + message.getFrom());

        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
    }
}
