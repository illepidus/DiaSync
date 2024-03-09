package ru.krotarnya.diasync.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FirebaseSendService extends Service {
    private static final String TAG = "FirebaseSendService";
    private static final String FIREBASE_API_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String FIREBASE_SERVER_KEY = "YOUR_SERVER_KEY";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };
    @Nullable
    private RequestQueue requestQueue;
    @Nullable
    private String accessToken;

    @Override
    public void onCreate() {
        requestQueue = Volley.newRequestQueue(this);
        try {
            accessToken = getAccessToken();
            Log.d(TAG, accessToken);
        } catch (IOException e) {
            accessToken = null;
        }
    }

    public void sendPushNotification(
            List<String> keys,
            String messageTitle,
            String message) throws JSONException
    {
        JSONObject msg = new JSONObject();

        msg.put("title", messageTitle);
        msg.put("body", message);
        msg.put("notificationType", "Test");
    }

    private static String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("firebase-service-account.json"))
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refreshIfExpired();
        AccessToken token = googleCredentials.getAccessToken();

        return token.getTokenValue();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
