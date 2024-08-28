package ru.krotarnya.diasync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ru.krotarnya.diasync.model.Libre2Value;
import ru.krotarnya.diasync.pip.PipActivity;
import ru.krotarnya.diasync.service.WearUpdateService;
import ru.krotarnya.diasync.widget.WidgetUpdateService;

public class DiaBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = DiaBroadcastReceiver.class.getSimpleName();
    private static final String LIBRE2_BG_INTENT_ACTION = "com.eveningoutpost.dexdrip.diasync.libre2_bg";
    private String webhookAddress;
    private String webhookToken;
    private Context broadcastContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        broadcastContext = context;
        final String action = intent.getAction();
        if (action == null) return;

        Bundle bundle = new Bundle(intent.getExtras());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean webhook_enabled = prefs.getBoolean("webhook_enabled", false);
        boolean webhook_enabled_follower = prefs.getBoolean("webhook_enabled_follower", false);
        webhookAddress = prefs.getString("webhook_address", "undefined");
        webhookToken = prefs.getString("webhook_token", "undefined");

        Log.d(TAG, "Received broadcast intent [" + action + "] in context [" + context + "]");
        if (action.equals(LIBRE2_BG_INTENT_ACTION)) {
            if (!bundle.containsKey("source") || !bundle.containsKey("libre2_value")) {
                Log.e(TAG, "Received faulty libre2_bg intent");
                return;
            }
            if (webhook_enabled) {
                String source = Optional.ofNullable(bundle.getString("source")).orElse("");
                if (source.equals("master") || (webhook_enabled_follower && source.equals("follower"))) {
                    webhookUpdate(bundle);
                }
            }

            Libre2Value libre2_value = new Libre2Value(bundle);
            Log.d(TAG, "Received: \n" + libre2_value);
            DiasyncDB diasync_db = DiasyncDB.getInstance(context);
            if (diasync_db.addLibre2Value(libre2_value)) {
                WidgetUpdateService.pleaseUpdate(context);
                WearUpdateService.pleaseUpdate(context);
                Alerter.check();
                Intent updatePipIntent = new Intent(PipActivity.UPDATE_ACTION);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updatePipIntent);
            }
            return;
        }

        Log.e(TAG, "Received unknown intent");
    }

    private void webhookUpdate(Bundle bundle) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new BundleTypeAdapterFactory());
        Gson gson = builder.create();
        webhookUpdate(gson.toJson(bundle));
    }

    private void webhookUpdate(String update) {
        RequestQueue request_queue = Volley.newRequestQueue(broadcastContext);
        Log.d(TAG, "Updating [" + webhookAddress + "]...");
        StringRequest string_request = new StringRequest(Request.Method.POST, webhookAddress, response -> Log.d(TAG, "Response: " + response), error -> Log.e(TAG, error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", webhookToken);
                params.put("update", update);
                params.put("type", "libre2_bg");
                return params;
            }
        };
        request_queue.add(string_request);
    }
}