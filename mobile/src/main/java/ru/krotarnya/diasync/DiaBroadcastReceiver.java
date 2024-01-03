package ru.krotarnya.diasync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ru.krotarnya.diasync.activity.PipActivity;
import ru.krotarnya.diasync.model.Libre2Value;
import ru.krotarnya.diasync.service.WidgetUpdateService;

public class DiaBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "DiaBroadcastReceiver";
    private static final String LIBRE2_BG_INTENT_ACTION = "com.eveningoutpost.dexdrip.diasync.libre2_bg";
    private static final String BLOOD_GLUCOSE_CAPABILITY = "blood_glucose";
    public static final String BLOOD_GLUCOSE_PATH = "/blood_glucose";
    private String  webhook_address;
    private String  webhook_token;
    private Context broadcast_context;

    @Override
    public void onReceive(Context context, Intent intent) {
        broadcast_context = context;
        final String action = intent.getAction();
        if (action == null) return;

        Bundle bundle = new Bundle(intent.getExtras());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean webhook_enabled = prefs.getBoolean("webhook_enabled", false);
        boolean webhook_enabled_follower = prefs.getBoolean("webhook_enabled_follower", false);
        webhook_address = prefs.getString("webhook_address", "undefined");
        webhook_token = prefs.getString("webhook_token", "undefined");

        Log.d (TAG, "Received broadcast intent [" + action + "] in context [" + context + "]");
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
            DiasyncDB diasync_db = DiasyncDB.getInstance(broadcast_context);
            diasync_db.addLibre2Value(libre2_value);

            Log.d(TAG, "Received: \n" + libre2_value);
            Alerter.check();
            WidgetUpdateService.pleaseUpdate(context);

            Intent updatePipIntent = new Intent(PipActivity.UPDATE_ACTION);
            LocalBroadcastManager.getInstance(context).sendBroadcast(updatePipIntent);

            resendToWear(libre2_value);

            return;
        }

        Log.e(TAG,"Received unknown intent");
    }

    private void resendToWear(Libre2Value libre2Value) {
        Log.d(TAG, "resending...");
        AsyncTask.execute(() -> {
            try {
                Tasks.await(Wearable
                                .getCapabilityClient(broadcast_context)
                                .getCapability(
                                        BLOOD_GLUCOSE_CAPABILITY,
                                        CapabilityClient.FILTER_REACHABLE))
                        .getNodes()
                        .stream()
                        .filter(Node::isNearby)
                        .forEach(node -> Wearable
                                .getMessageClient(broadcast_context)
                                .sendMessage(
                                        node.getId(),
                                        BLOOD_GLUCOSE_PATH,
                                        String.valueOf(libre2Value.getMmolValue()).getBytes()));
            }
            catch (Exception e) {
                Log.e(TAG, "Something went wrong sending data to wear", e);
            }
        });
    }

    private void webhookUpdate(Bundle bundle) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new BundleTypeAdapterFactory());
        Gson gson = builder.create();
        webhookUpdate(gson.toJson(bundle));
    }

    private void webhookUpdate(String update) {
        RequestQueue request_queue = Volley.newRequestQueue(broadcast_context);
        Log.d(TAG, "Updating [" + webhook_address + "]...");
        StringRequest string_request = new StringRequest(Request.Method.POST, webhook_address, response -> Log.d(TAG, "Response: " + response), error -> Log.e(TAG, error.toString())) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("token",  webhook_token);
                params.put("update", update);
                params.put("type", "libre2_bg");
                return params;
            }
        };
        request_queue.add(string_request);
    }
}