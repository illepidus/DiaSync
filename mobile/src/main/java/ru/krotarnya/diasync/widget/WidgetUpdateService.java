package ru.krotarnya.diasync.widget;

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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import ru.krotarnya.diasync.Diasync;

public final class WidgetUpdateService extends Service {
    private static final String TAG = WidgetUpdateService.class.getSimpleName();
    private static final Set<String> INACTIVE_STATE_ACTIONS = Set.of(
            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_SCREEN_OFF);
    private static final Set<String> ACTIVE_STATE_ACTIONS = Set.of(
            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_SCREEN_OFF,
            Intent.ACTION_TIME_TICK,
            Diasync.Intents.NEW_DATA_AVAILABLE);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Optional.ofNullable(intent.getAction()).orElse("")) {
                case Intent.ACTION_TIME_TICK:
                case Diasync.Intents.NEW_DATA_AVAILABLE:
                    updateWidgets();
                    break;
                case Intent.ACTION_SCREEN_ON:
                    setServiceState(true, true);
                    updateWidgets();
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    setServiceState(false, true);
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Service.POWER_SERVICE);
        setServiceState(pm.isInteractive(), false);
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
    }

    private void setServiceState(boolean isActive, boolean isRegistered) {
        IntentFilter intentFilter = new IntentFilter();
        Collection<String> actions = isActive ? ACTIVE_STATE_ACTIONS : INACTIVE_STATE_ACTIONS;
        actions.forEach(intentFilter::addAction);
        if (isRegistered) unregisterReceiver(broadcastReceiver);
        registerReceiver(broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED);
    }

    private void updateWidgets() {
        Intent intent = new Intent(getApplicationContext(), DiasyncWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplicationContext())
                .getAppWidgetIds(new ComponentName(getApplicationContext(), DiasyncWidget.class));
        if (ids.length > 0) {
            Log.d(TAG, "Updating Diasync widgets");
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    }
}