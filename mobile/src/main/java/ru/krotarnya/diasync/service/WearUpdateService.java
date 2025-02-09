package ru.krotarnya.diasync.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ru.krotarnya.diasync.DiasyncDB;
import ru.krotarnya.diasync.Glucose;
import ru.krotarnya.diasync.common.model.WatchFaceBloodData;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodGlucoseUnit;
import ru.krotarnya.diasync.common.model.BloodPoint;
import ru.krotarnya.diasync.model.Libre2ValueList;

public class WearUpdateService extends Service {
    private static final String TAG = "WearUpdateService";
    private static final String CAPABILITY = "blood_chart";
    public static final String CAPABILITY_PATH = "/blood_chart";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWear();
        return START_STICKY;
    }

    private void updateWear() {
        Log.d(TAG, "looking for nearby nodes...");
        try {
            Wearable.getCapabilityClient(getApplicationContext())
                    .getCapability(CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Wasn't able to communicate to device");
                            return;
                        }
                        Log.d(TAG, "found " + task.getResult().getNodes().size() + " nodes");
                        task.getResult()
                                .getNodes()
                                .stream()
                                .peek(x -> Log.d(TAG, "node: " + x.getDisplayName()))
                                .filter(Node::isNearby)
                                .forEach(node -> Wearable
                                        .getMessageClient(this)
                                        .sendMessage(node.getId(), CAPABILITY_PATH, getBloodData()));
                    });
        } catch (Exception exception) {
            Log.w(TAG, "Something went terribly wrong. Is wearable API available?", exception);
        }
    }

    private byte[] getBloodData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long graph_period = Long.parseLong(prefs.getString("watchface_graph_period", "1800000"));
        long t2 = System.currentTimeMillis(), t1 = t2 - graph_period;

        DiasyncDB diasync_db = DiasyncDB.getInstance(this);
        Libre2ValueList libre2Values = diasync_db.getLibre2Values(t1, t2 + 60000);
        BloodGlucoseUnit unit = BloodGlucoseUnit.resolveOrThrow(prefs.getString("glucose_units", "mmol"));

        List<BloodPoint> points = libre2Values.stream()
                .map(v -> new BloodPoint(
                        Instant.ofEpochMilli(v.timestamp),
                        BloodGlucose.consMgdl(v.getValue())))
                .collect(Collectors.toList());
        WatchFaceBloodData.Params params = new WatchFaceBloodData.Params(
                unit,
                BloodGlucose.consMgdl(Glucose.low()),
                BloodGlucose.consMgdl(Glucose.high()),
                Duration.ofMillis(graph_period),
                new WatchFaceBloodData.Colors(
                        Glucose.lowGraphColor(),
                        Glucose.normalGraphColor(),
                        Glucose.highGraphColor(),
                        Glucose.lowTextColor(),
                        Glucose.normalTextColor(),
                        Glucose.highTextColor()));

        return new WatchFaceBloodData(points, libre2Values.getTrendArrow(), params).serialize();
    }

    public static void pleaseUpdate(Context context) {
        try {
            context.startService(new Intent(context, WearUpdateService.class));
            Log.d(TAG, "Starting service in context [" + context + "]");
        } catch (Exception e) {
            Log.d(TAG, "Failed to start service in context [" + context + "]", e);
        }
    }
}
