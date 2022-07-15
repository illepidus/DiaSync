package com.krotarnya.diasync;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class DiaBroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG = "DiaBroadcastReceiver";
    private String  webhook_address;
    private String  webhook_token;
    private Context broadcast_context;

    @Override
    public void onReceive(Context context, Intent intent) {
        broadcast_context = context;
        final String action = intent.getAction();
        Bundle bundle = new Bundle(intent.getExtras());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean webhook_enabled = prefs.getBoolean("webhook_enabled", false);
        boolean webhook_enabled_follower = prefs.getBoolean("webhook_enabled_follower", false);
        webhook_address = prefs.getString("webhook_address", "undefined");
        webhook_token = prefs.getString("webhook_token", "undefined");

        if (action == null) return;

        Log.d (TAG, "Received broadcast intent [" + action + "] in context [" + context.toString() + "]");
        if (action.equals("com.eveningoutpost.dexdrip.diasync.libre2_bg")) {
            if (!bundle.containsKey("source") || !bundle.containsKey("libre2_value")) {
                Log.e(TAG, "Received faulty libre2_bg intent");
                return;
            }
            if (webhook_enabled) {
                if (bundle.getString("source").equals("master") || (webhook_enabled_follower && bundle.getString("source").equals("follower"))) {
                    webhookUpdate(bundle, "libre2_bg");
                }
            }

            Libre2Value libre2_value = new Libre2Value(bundle);
            DiasyncDB diasync_db = DiasyncDB.getInstance(broadcast_context);
            diasync_db.addLibre2Value(libre2_value);

            Intent widget_intent = new Intent(context, Libre2Widget.class);
            widget_intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int [] ids = AppWidgetManager.getInstance(context.getApplicationContext())
                    .getAppWidgetIds(new ComponentName(context.getApplicationContext(), Libre2Widget.class));
            if (ids.length > 0) {
                widget_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                context.sendBroadcast(widget_intent);
            }
            return;
        }

        Log.e(TAG,"Received unknown intent");
    }

    private void webhookUpdate(Bundle bundle, String type) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new BundleTypeAdapterFactory());
        Gson gson = builder.create();
        webhookUpdate(gson.toJson(bundle), type);
    }

    private void webhookUpdate(String update, String type) {
        RequestQueue request_queue = Volley.newRequestQueue(broadcast_context);
        Log.d(TAG, "Updating [" + webhook_address + "]...");
        StringRequest string_request = new StringRequest(Request.Method.POST, webhook_address, response -> Log.d(TAG, "Response: " + response), error -> Log.e(TAG, error.toString())) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("token",  webhook_token);
                params.put("update", update);
                params.put("type",   type);
                return params;
            }
        };
        request_queue.add(string_request);
    }
}