package ru.krotarnya.diasync.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import ru.krotarnya.diasync.Libre2Widget;

public class WidgetUpdateService extends Service {
    private static final String TAG = "WidgetUpdateService";
    private boolean isRegistered = false;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                updateWidgets();
            } else if (intent.getAction().compareTo(Intent.ACTION_SCREEN_ON) == 0) {
                enableClockTicks();
                updateWidgets();
            } else if (intent.getAction().compareTo(Intent.ACTION_SCREEN_OFF) == 0) {
                disableClockTicks();
            }
        }
    };

    public WidgetUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) { throw new UnsupportedOperationException("Not yet implemented"); }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Service.POWER_SERVICE);
        Log.d(TAG, "Created");
        if (pm.isInteractive())
            enableClockTicks();
        else
            disableClockTicks();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWidgets();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        isRegistered = false;
    }

    private void enableClockTicks() {
        Log.d(TAG, "enableClockTicks");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        if (isRegistered)
            unregisterReceiver(broadcastReceiver);
        registerReceiver(broadcastReceiver, intentFilter);
        isRegistered = true;
    }

    private void disableClockTicks() {
        Log.d(TAG, "disableClockTicks");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        if (isRegistered)
            unregisterReceiver(broadcastReceiver);
        registerReceiver(broadcastReceiver, intentFilter);
        isRegistered = true;
    }

    public static void pleaseUpdate(Context context) {
        try {
            context.startService(new Intent(context, WidgetUpdateService.class));
            Log.d(TAG, "Starting service in context [" + context + "]");
        } catch (Exception e) {
            Log.d(TAG, "Failed to start service in context [" + context + "]");
            e.printStackTrace();
        }
    }

    private void updateWidgets() {
        Intent widget_intent = new Intent(getApplicationContext(), Libre2Widget.class);
        widget_intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int [] ids = AppWidgetManager.getInstance(getApplicationContext())
                .getAppWidgetIds(new ComponentName(getApplicationContext(), Libre2Widget.class));
        if (ids.length > 0) {
            widget_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            Log.d(TAG, "Updating widgets");
            getApplicationContext().sendBroadcast(widget_intent);
        }
    }
}