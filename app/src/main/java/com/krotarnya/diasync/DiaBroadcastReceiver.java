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
    private static final String TAG = "DiaBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String url = "https://krotarnya.ru/diasync.php";
        final String token = "78e297d6d6a5fe57da3184705551a69a4";
        final String action = intent.getAction();
        final Bundle bundle = intent.getExtras();

        String update;
        String type;

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new BundleTypeAdapterFactory());
        Gson gson = builder.create();

        if (action == null) return;

        Log.d (TAG, "Received broadcast intent [" + action + "] with following extras: ");
        for (String key: bundle.keySet()) {
            Log.d (TAG, "\"" + key + "\" => [" + bundle.get(key).getClass().getSimpleName() + "]");
        }

        switch (action) {
            case "com.eveningoutpost.dexdrip.diasync.libre2_activation":
                if (!bundle.containsKey("sensor") || !bundle.containsKey("bleManager")) {
                    Log.e(TAG,"Received faulty libre2_activation intent");
                    return;
                }
                update = gson.toJson(bundle);
                type = "libre2_activation";
                break;
            case "com.eveningoutpost.dexdrip.diasync.libre2_bg":
                if (!bundle.containsKey("glucose") || !bundle.containsKey("timestamp") || !bundle.containsKey("bleManager")) {
                    Log.e(TAG,"Received faulty libre2_bg intent");
                    return;
                }
                update = gson.toJson(bundle);
                type = "libre2_bg";
                break;
            case "com.eveningoutpost.dexdrip.diasync.l2r":
                //REMOVE THIS LATER
                update = gson.toJson(bundle);
                type = "REMOVE_THIS_TYPE_OF_UPDATES";
                break;
            default:
                Log.e(TAG, "Unknown action: " + action);
                return;
        }

        RequestQueue request_queue = Volley.newRequestQueue(context);
        StringRequest string_request = new StringRequest(Request.Method.POST, url, response -> Log.d(TAG, "Response: " + response), error -> Log.e(TAG, error.toString())) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("token", token);
                params.put("update", update);
                params.put("type", type);
                return params;
            }
        };

        request_queue.add(string_request);
    }
}
