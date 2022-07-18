package com.krotarnya.diasync;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class Libre2Widget extends AppWidgetProvider {
    private static final String TAG = "Libre2Widget";
    private static final String WIDGET_CLICKED_TAG = "WIDGET_CLICKED";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_libre2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String glucose_units = prefs.getString("glucose_units", "mmol");

        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2Value libre2_value = diasync_db.getLastLibre2Value();
        long t2 = System.currentTimeMillis(), t1 = t2 - 3600000;
        Libre2ValueList libre2_values = diasync_db.getLibre2Values(t1, t2);

        views.setImageViewBitmap(R.id.libre2_widget_graph, new Libre2GraphBuilder(context)
            .setWidth(appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH))
            .setHeight(appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT))
            .setXMin(t1 - 120000)
            .setXMax(t2 + 120000)
            .setYMin(Double.min((libre2_values.minCalibratedValue().getCalibratedValue()), Glucose.low()) - 18)
            .setYMax(Double.max((libre2_values.maxCalibratedValue().getCalibratedValue()), Glucose.high()) + 18)
            .setData(libre2_values)
            .build());

        switch (glucose_units) {
            case "mmol":
                views.setTextViewText(R.id.libre2_widget_glucose, Glucose.stringMmol(libre2_value.getCalibratedMmolValue()));
                break;
            case "mgdl":
                views.setTextViewText(R.id.libre2_widget_glucose, Glucose.stringMgdl(libre2_value.getCalibratedValue()));
                break;
            default:
                Log.wtf(TAG, "Unknown glucose units");
                views.setTextViewText(R.id.libre2_widget_glucose, "----");
        }

        views.setTextViewText(R.id.data_timer, String.valueOf(SimpleDateFormat.getInstance().format(libre2_value.timestamp)));
        views.setTextColor(R.id.libre2_widget_glucose, Glucose.bloodTextColor(libre2_value.getCalibratedValue()));
        views.setOnClickPendingIntent(R.id.libre2_widget_layout, getPendingSelfIntent(context, WIDGET_CLICKED_TAG));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.d(TAG, "Received new intent action = " + action);
        if (Objects.equals(action, WIDGET_CLICKED_TAG)) {
            Intent settingsIntent = new Intent(Intent.ACTION_VIEW);
            settingsIntent.setClassName(context.getPackageName(), DiasyncSettings.class.getName());
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);
        }
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, Libre2Widget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
