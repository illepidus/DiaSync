package com.krotarnya.diasync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaBroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG      = "DiaBroadcastReceiver";
    private static final String ds_token = "78e297d6d6a5fe57da3184705551a69a";
    private static final String ds_url    = "https://krotarnya.ru/diasync.php";
    private Context broadcast_context;

    @Override
    public void onReceive(Context context, Intent intent) {
        broadcast_context = context;
        final String action = intent.getAction();
        Bundle bundle = new Bundle(intent.getExtras());

        if (action == null) return;

        Log.d (TAG, "Received broadcast intent [" + action + "] in context [" + context.toString() + "]");
        if (action.equals("com.eveningoutpost.dexdrip.diasync.libre2_bg")) {
            if (!bundle.containsKey("source") || !bundle.containsKey("libre2_value")) {
                Log.e(TAG, "Received faulty libre2_bg intent");
                return;
            }
            if (bundle.getString("source").equals("master")) sendUpdate(bundle, "libre2_bg");

            Libre2Value libre2_value = new Libre2Value(bundle);
            DiasyncDB diasync_db = DiasyncDB.getInstance(broadcast_context);
            diasync_db.addLibre2Value(libre2_value);
            return;
        }

        Log.e(TAG,"Received unknown intent");
    }

    private void sendUpdate(Bundle bundle, String type) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new BundleTypeAdapterFactory());
        Gson gson = builder.create();
        sendUpdate(gson.toJson(bundle), type);
    }

    private void sendUpdate(String update, String type) {
        RequestQueue request_queue = Volley.newRequestQueue(broadcast_context);
        Log.d(TAG, "Sending update to [" + ds_url + "]...");
        StringRequest string_request = new StringRequest(Request.Method.POST, ds_url, response -> Log.d(TAG, "Response: " + response), error -> Log.e(TAG, error.toString())) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("token",  ds_token);
                params.put("update", update);
                params.put("type",   type);
                return params;
            }
        };
        request_queue.add(string_request);
    }
}