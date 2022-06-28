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
import java.util.Map;

public class DiaBroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG      = "DiaBroadcastReceiver";
    private static final String ds_token = "78e297d6d6a5fe57da3184705551a69a4";
    private static final String ds_url    = "https://krotarnya.ru/diasync.php";
    private Context broadcast_context;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final Bundle bundle = intent.getExtras();
        broadcast_context = context;

        if (action == null) return;

        Log.d (TAG, "Received broadcast intent [" + action + "] in context [" + context.toString() + "] with following extras: ");
        for (String key: bundle.keySet()) {
            Log.d (TAG, "\"" + key + "\" => [" + bundle.get(key).getClass().getSimpleName() + "]");
        }

        if (action.equals("com.eveningoutpost.dexdrip.diasync.libre2_activation")) {
            if (!bundle.containsKey("sensor") || !bundle.containsKey("bleManager")) {
                Log.e(TAG,"Received faulty libre2_activation intent");
                return;
            }
            sendUpdate(bundle, "libre2_activation");
            return;
        }

        if (action.equals("com.eveningoutpost.dexdrip.diasync.libre2_bg")) {
            if (!bundle.containsKey("glucose") || !bundle.containsKey("timestamp") || !bundle.containsKey("bleManager")) {
                Log.e(TAG, "Received faulty libre2_bg intent");
                return;
            }
            sendUpdate(bundle, "libre2_bg");
        }
        if (action.equals("com.eveningoutpost.dexdrip.diasync.libre2_bg_follower")) {
            return;
        }
        if (action.equals("com.eveningoutpost.dexdrip.diasync.l2r")) {
            sendUpdate(bundle, "DELETE_ME");
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
        Log.d(TAG, "Sending update to " + ds_url + "...");
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
