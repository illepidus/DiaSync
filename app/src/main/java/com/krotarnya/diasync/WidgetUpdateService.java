package com.krotarnya.diasync;

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

public class WidgetUpdateService extends Service {
    private static final String TAG = "WidgetUpdateService";
    private boolean isRegistered = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final PowerManager.WakeLock wl = getWakeLock("diasync-widget-broadcast", 20000);
            if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                updateWidgets();
            } else if (intent.getAction().compareTo(Intent.ACTION_SCREEN_ON) == 0) {
                enableClockTicks();
                updateWidgets();
            } else if (intent.getAction().compareTo(Intent.ACTION_SCREEN_OFF) == 0) {
                disableClockTicks();
            }
            releaseWakeLock(wl);
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
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            isRegistered = false;
        }
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

    public static PowerManager.WakeLock getWakeLock(final String name, int millis) {
        final PowerManager pm = (PowerManager) Diasync.getContext().getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, name);
        wl.acquire(millis);
        Log.d(TAG, "getWakeLock: " + name + " " + wl.toString());
        return wl;
    }

    public static synchronized void releaseWakeLock(final PowerManager.WakeLock wl) {
        Log.d(TAG, "releaseWakeLock: " + wl.toString());
        if (wl == null) return;
        if (wl.isHeld()) {
            try {
                wl.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing wakelock: " + e);
            }
        }
    }

    public static void start(Context context) {
        if (AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, Libre2Widget.class)).length == 0) {
            Log.d(TAG, "No widgets exists in context[" + context + "] wherefore no need to update them");
            return;
        }
        try {
            context.startService(new Intent(context, WidgetUpdateService.class));
            Log.d(TAG, "Starting service in context [" + context + "]");
        } catch (Exception e) {
            Log.d(TAG, "Failed to start service in context [" + context + "]");
            e.printStackTrace();
        }
    }

    public void updateWidgets() {
        Log.d(TAG, "Updating widgets");
        Libre2Widget.update(getApplication());
    }
}