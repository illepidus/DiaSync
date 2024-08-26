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

import ru.krotarnya.diasync.DiasyncGraphBuilder;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.common.model.BloodData;
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
        Log.d(TAG, "onUpdate");
        Arrays.stream(appWidgetIds).forEach(id -> update(context, appWidgetManager, id));
    }

    @Override
    public void onAppWidgetOptionsChanged(
            Context context,
            AppWidgetManager appWidgetManager,
            int appWidgetId,
            Bundle newOptions)
    {
        Log.d(TAG, "onAppWidgetOptionsChanged call " + newOptions);
        update(context, appWidgetManager, appWidgetId);
    }

    /**
     * Do not use this other than processing on click self intents
     * Widget setOnClickIntents should be processed by onUpdate call
     */
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
        setOnClickIntents(context, views);
        // TODO: implement
        BloodData data = null;
        drawGraph(data, views, appWidgetManager.getAppWidgetOptions(id));
        appWidgetManager.updateAppWidget(id, views);
    }

    private void drawGraph(BloodData data, RemoteViews views, Bundle options) {
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        views.setImageViewBitmap(R.id.diasync_widget_canvas, new DiasyncGraphBuilder()
                .setWidth(width)
                .setHeight(height)
                .setData(data)
                .build());
    }

    private void setOnClickIntents(Context context, RemoteViews views) {
        setOnClickIntent(context, views, R.id.diasync_widget_canvas, ACTION_XDRIP);
        setOnClickIntent(context, views, R.id.diasync_widget_alerts, ACTION_ALERTS);
        setOnClickIntent(context, views, R.id.diasync_widget_pip, ACTION_PIP);
    }

    private void setOnClickIntent(
            Context context,
            RemoteViews views,
            @IdRes int viewId,
            String action)
    {
        views.setOnClickPendingIntent(viewId, getPendingSelfIntent(context, action));
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, DiasyncWidget.class).setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
