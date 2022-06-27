package com.krotarnya.diasync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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

        if (action == null) return;

        Log.d (TAG, "Received broadcast intent [" + action + "] with following extras: ");
        for (String key: bundle.keySet()) {
            Log.d (TAG, "\"" + key + "\" => [" + bundle.get(key).getClass().getSimpleName() + "]");
        }

        switch (action) {
            case "com.eveningoutpost.dexdrip.diasync.libre2_activation":
                update = "NULL";
                type = "libre2_activation";
                break;
            case "com.eveningoutpost.dexdrip.diasync.libre2_bg":
                update = "NULL";
                type = "libre2_bg";
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
