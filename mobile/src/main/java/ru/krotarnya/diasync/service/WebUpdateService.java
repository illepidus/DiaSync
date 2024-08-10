package ru.krotarnya.diasync.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import ru.krotarnya.diasync.Alerter;
import ru.krotarnya.diasync.Diasync;
import ru.krotarnya.diasync.DiasyncDB;
import ru.krotarnya.diasync.activity.PipActivity;
import ru.krotarnya.diasync.model.Libre2Update;
import ru.krotarnya.diasync.model.Libre2Value;

public class WebUpdateService extends Service {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final int FOREGROUND_ID = 0x19CA5000;
    private static final String NOTIFICATION_CHANNEL_ID = "DiasyncNotificationChannel";


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
                    if (diasync_db.addLibre2Value(libre2_value)) {
                        WidgetUpdateService.pleaseUpdate(context);
                        WearUpdateService.pleaseUpdate(context);
                        Alerter.check();
                        Intent updatePipIntent = new Intent(PipActivity.UPDATE_ACTION);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(updatePipIntent);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "update failed", e);
                }
            }
        }, 0, 10000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FOREGROUND_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        }
        else {
            startForeground(FOREGROUND_ID, buildNotification());
        }
        return START_STICKY;
    }

    private Notification buildNotification() {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Diasync channel for foreground service notification");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle("Diasync")
                .setContentText("Working...")
                .build();
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
}
