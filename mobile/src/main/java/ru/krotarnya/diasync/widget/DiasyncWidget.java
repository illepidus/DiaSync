package ru.krotarnya.diasync.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;

import java.util.Arrays;
import java.util.Optional;

import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.settings.AlertsFragment;
import ru.krotarnya.diasync.settings.SettingsActivity;

/**
 * @noinspection SameParameterValue
 */
public final class DiasyncWidget extends AppWidgetProvider {
    private static final String TAG = DiasyncWidget.class.getSimpleName();
    private static final String XDRIP_PACKAGE_NAME = "com.eveningoutpost.dexdrip";
    private static final String ACTION_DEFAULT = "ACTION_DEFAULT";
    private static final String ACTION_XDRIP = "ACTION_XDRIP";
    private static final String ACTION_ALERTS = "ACTION_ALERTS";
    private static final String ACTION_PIP = "ACTION_PIP";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Arrays.stream(appWidgetIds).forEach(id -> update(context, appWidgetManager, id));
    }

    @Override
    public void onAppWidgetOptionsChanged(
            Context context,
            AppWidgetManager appWidgetManager,
            int appWidgetId,
            Bundle newOptions) {
        update(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Received " + intent);
        switch (Optional.ofNullable(intent.getAction()).orElse(ACTION_DEFAULT)) {
            case ACTION_ALERTS:
                SettingsActivity.pleaseStartExternally(context, AlertsFragment.class);
                break;
            case ACTION_XDRIP:
                Optional.ofNullable(context.getPackageManager().getLaunchIntentForPackage(XDRIP_PACKAGE_NAME))
                        .map(i -> i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK))
                        .ifPresent(context::startActivity);
                break;
            case ACTION_PIP:
                break;
        }


    }

    private void update(Context context, AppWidgetManager appWidgetManager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diasync_widget);
        update(context, views);
        appWidgetManager.updateAppWidget(id, views);
    }

    private void update(Context context, RemoteViews views) {
        views.setImageViewResource(R.id.diasync_widget_canvas, android.R.color.holo_red_dark);
        setOnClickIntent(context, views, R.id.diasync_widget_canvas, ACTION_XDRIP);
        setOnClickIntent(context, views, R.id.diasync_widget_alerts, ACTION_ALERTS);
        setOnClickIntent(context, views, R.id.diasync_widget_pip, ACTION_PIP);
    }

    private void setOnClickIntent(Context context, RemoteViews views, @IdRes int viewId, String action) {
        views.setOnClickPendingIntent(viewId, getPendingSelfIntent(context, action));
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, DiasyncWidget.class).setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
