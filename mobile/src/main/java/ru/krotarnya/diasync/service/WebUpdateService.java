package ru.krotarnya.diasync.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ru.krotarnya.diasync.Alerter;
import ru.krotarnya.diasync.Diasync;
import ru.krotarnya.diasync.DiasyncDB;
import ru.krotarnya.diasync.activity.PipActivity;
import ru.krotarnya.diasync.model.Libre2Value;

public class WebUpdateService extends Service {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String TAG = "WebUpdateService";
    private final Timer timer = new Timer();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Started");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Context context = Diasync.getContext();
                    Libre2Value libre2_value = getLastLibre2Value();
                    Log.d(TAG, "Received: \n" + libre2_value);
                    DiasyncDB diasync_db = DiasyncDB.getInstance(context);
                    diasync_db.addLibre2Value(libre2_value);

                    Alerter.check();
                    WidgetUpdateService.pleaseUpdate(context);
                    WearUpdateService.pleaseUpdate(context);

                    Intent updatePipIntent = new Intent(PipActivity.UPDATE_ACTION);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(updatePipIntent);
                } catch (Exception e) {
                    Log.w(TAG, "update failed", e);
                }
            }
        }, 0, 10000);
    }

    private Libre2Value getLastLibre2Value() throws Exception {
        StringBuilder sb = new StringBuilder();
        URL url = new URL("https://krotarnya.ru/diasync.php");

        BufferedReader in;
        in = new BufferedReader(
                new InputStreamReader(
                        url.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            sb.append(inputLine);

        in.close();

        return OBJECT_MAPPER.readValue(sb.toString(), Libre2Update.class).toLibre2Value();
    }

    private static class Libre2Update {
        Long xdrip_timestamp;
        String xdrip_arrow;
        Double xdrip_value;
        String libre2_serial;
        String source;
        Double libre2_value;
        String xdrip_sync_key;
        Long libre2_timestamp;
        Double xdrip_calibration_slope;
        Double xdrip_calibration_intercept;
        Long xdrip_calibration_timestamp;


        public Libre2Value toLibre2Value() {
            Intent intent = new Intent();
            intent.putExtra("source", source);
            intent.putExtra("libre2_serial", libre2_serial);
            intent.putExtra("libre2_value", libre2_value);
            intent.putExtra("libre2_timestamp", libre2_timestamp);
            intent.putExtra("xdrip_sync_key", xdrip_sync_key);
            if (xdrip_calibration_slope != null)
                intent.putExtra("xdrip_calibration_slope", xdrip_calibration_slope);
            if (xdrip_calibration_intercept != null)
                intent.putExtra("xdrip_calibration_intercept", xdrip_calibration_intercept);
            if (xdrip_calibration_timestamp != null)
                intent.putExtra("xdrip_calibration_timestamp", xdrip_calibration_timestamp);
            intent.putExtra("xdrip_value", xdrip_value);
            intent.putExtra("xdrip_timestamp", xdrip_timestamp);
            intent.putExtra("xdrip_arrow", xdrip_arrow);

            return new Libre2Value(Objects.requireNonNull(intent.getExtras()));
        }
    }
}
