package ru.krotarnya.diasync.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
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
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.krotarnya.diasync.Alerter;
import ru.krotarnya.diasync.Diasync;
import ru.krotarnya.diasync.DiasyncDB;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.model.Libre2Update;
import ru.krotarnya.diasync.model.Libre2Value;

public class WebUpdateService extends Service {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final int FOREGROUND_ID = 0x19CA5000;
    private static final String NOTIFICATION_CHANNEL_ID = "DiasyncNotificationChannel";
    private static final String TAG = WebUpdateService.class.getSimpleName();
    private final Timer timer = new Timer();
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                PendingIntent.getService(
                        getApplicationContext(),
                        1,
                        new Intent(getApplicationContext(), this.getClass())
                                .setPackage(getPackageName()),
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isStarted.getAndSet(true)) {
            startForeground(
                    FOREGROUND_ID,
                    buildForegroundNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            timer.scheduleAtFixedRate(new WebUpdateTask(this), 0, 10000);
        }
        return START_STICKY;
    }

    private Notification buildForegroundNotification() {
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
                .setContentText("Diasync update service")
                .setSmallIcon(R.drawable.ic_connectivity_settings)
                .build();
    }

    private Libre2Value getLastLibre2Value() throws Exception {
        StringBuilder sb = new StringBuilder();
        URL url = new URL("https://krotarnya.ru/diasync.php");

        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(url.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            sb.append(inputLine);

        in.close();

        return OBJECT_MAPPER.readValue(sb.toString(), Libre2Update.class).toLibre2Value();
    }

    private class WebUpdateTask extends TimerTask {
        private final Context context;

        private WebUpdateTask(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            try {
                Libre2Value libre2_value = getLastLibre2Value();
                DiasyncDB diasyncDB = DiasyncDB.getInstance(context);
                Alerter.check();
                if (diasyncDB.addLibre2Value(libre2_value)) {
                    Intent intent = new Intent(Diasync.Intents.NEW_DATA_AVAILABLE);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    WearUpdateService.pleaseUpdate(context);
                }
            } catch (Exception e) {
                Log.w(TAG, "Update failed " + e.getMessage(), e);
            }
        }
    }

    public static class ServiceRunner extends BroadcastReceiver {
        private static final Set<String> PERMITTED_ACTIONS = Set.of(Intent.ACTION_BOOT_COMPLETED);

        @Override
        public void onReceive(Context context, Intent intent) {
            Optional.ofNullable(intent.getAction())
                    .filter(PERMITTED_ACTIONS::contains)
                    .ifPresent(any -> context.startForegroundService(
                            new Intent(context, WebUpdateService.class)));
        }
    }
}
