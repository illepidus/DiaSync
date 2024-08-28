package ru.krotarnya.diasync.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.preference.PreferenceManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import ru.krotarnya.diasync.DiasyncDB;
import ru.krotarnya.diasync.DiasyncGraphBuilder;
import ru.krotarnya.diasync.Glucose;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.common.model.BloodData;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodGlucoseUnit;
import ru.krotarnya.diasync.common.model.BloodPoint;
import ru.krotarnya.diasync.common.model.TrendArrow;
import ru.krotarnya.diasync.model.Libre2ValueList;
import ru.krotarnya.diasync.service.WebUpdateService;
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

        context.startService(new Intent(context, WebUpdateService.class));
    }

    private void update(Context context, AppWidgetManager appWidgetManager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diasync_widget);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        BloodData bloodData = getBloodData(context);
        Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(id);
        Configuration configuration = context.getResources().getConfiguration();

        setOnClickIntents(context, views);
        drawGraph(bloodData, configuration, displayMetrics, appWidgetOptions, views);
        appWidgetManager.updateAppWidget(id, views);
    }

    private BloodData getBloodData(Context context) {
        // TODO: upgrade
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long graph_period = Long.parseLong(prefs.getString("widget_graph_period", "1800000"));
        long t2 = System.currentTimeMillis(), t1 = t2 - graph_period;

        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2ValueList libre2Values = diasync_db.getLibre2Values(t1, t2 + 60000);
        BloodGlucoseUnit unit = BloodGlucoseUnit.resolveOrThrow(prefs.getString("glucose_unit", "mmol"));

        List<BloodPoint> points = libre2Values.stream()
                .map(v -> new BloodPoint(
                        Instant.ofEpochMilli(v.timestamp),
                        BloodGlucose.consMgdl(v.getValue())))
                .collect(Collectors.toList());
        BloodData.Params params = new BloodData.Params(
                unit,
                BloodGlucose.consMgdl(Glucose.low()),
                BloodGlucose.consMgdl(Glucose.high()),
                Duration.ofMillis(graph_period),
                new BloodData.Colors(
                        Glucose.widgetBackgroundColor(),
                        Glucose.lowGraphColor(),
                        Glucose.normalGraphColor(),
                        Glucose.highGraphColor(),
                        Glucose.lowTextColor(),
                        Glucose.normalTextColor(),
                        Glucose.highTextColor(),
                        Glucose.errorTextColor(),
                        Glucose.lowGraphZoneColor(),
                        Glucose.normalGraphZoneColor(),
                        Glucose.highGraphZoneColor()));

        return new BloodData(points, TrendArrow.of(points), params);
    }

    private void drawGraph(
            BloodData data,
            Configuration configuration,
            DisplayMetrics displayMetrics,
            Bundle options,
            RemoteViews views)
    {
        int orientation = configuration.orientation;
        Function<String, Integer> dipsToPixels = dips -> Math.round(options.getInt(dips) * displayMetrics.density);
        int width = dipsToPixels.apply(orientation == Configuration.ORIENTATION_LANDSCAPE
                                               ? AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
                                               : AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int height = dipsToPixels.apply(orientation == Configuration.ORIENTATION_LANDSCAPE
                                                ? AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
                                                : AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        views.setImageViewBitmap(R.id.diasync_widget_canvas, new DiasyncGraphBuilder()
                .setData(data)
                .setWidth(width)
                .setHeight(height)
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
