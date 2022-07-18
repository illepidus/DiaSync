package com.krotarnya.diasync;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Libre2Widget extends AppWidgetProvider {
    private static final String TAG = "Libre2Widget";
    private static final String WIDGET_CLICKED_TAG = "WIDGET_CLICKED";

    @SuppressLint("ResourceType")
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_libre2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String glucose_units = prefs.getString("glucose_units", "mmol");
        Double glucose_low  = (double) prefs.getFloat("glucose_low_mgdl", 70.f);
        Double glucose_high = (double) prefs.getFloat("glucose_high_mgdl", 120.f);

        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2Value libre2_value = diasync_db.getLastLibre2Value();
        long t2 = System.currentTimeMillis(), t1 = t2 - 1800000;
        Libre2ValueList libre2_values = diasync_db.getLibre2Values(t1, t2);

        Libre2GraphBuilder libre2_graph_builder = new Libre2GraphBuilder(context);
        libre2_graph_builder.setWidth(appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH));
        libre2_graph_builder.setHeight(appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
        libre2_graph_builder.setXMin(t1 - 100000);
        libre2_graph_builder.setXMax(t2 + 100000);
        libre2_graph_builder.setYMin(Double.min((libre2_values.minCalibratedValue().getCalibratedValue()), glucose_low) - 20);
        libre2_graph_builder.setYMax(Double.max((libre2_values.maxCalibratedValue().getCalibratedValue()), glucose_high) + 20);
        libre2_graph_builder.setData(libre2_values);
        views.setImageViewBitmap(R.id.libre2_widget_graph, libre2_graph_builder.build());

        switch (glucose_units) {
            case "mmol":
                views.setTextViewText(R.id.blood_glucose, Glucose.stringMmol(libre2_value.getCalibratedMmolValue()));
                break;
            case "mgdl":
                views.setTextViewText(R.id.blood_glucose, Glucose.stringMgdl(libre2_value.getCalibratedValue()));
                break;
            default:
                Log.wtf(TAG, "Unknown glucose units");
                views.setTextViewText(R.id.blood_glucose, "----");
        }
        Date date = new Date(libre2_value.timestamp);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat date_format = new SimpleDateFormat("HH:mm");
        views.setTextViewText(R.id.data_timer, String.valueOf(date_format.format(libre2_value.timestamp)));
        views.setTextColor(R.id.blood_glucose, Glucose.bloodTextColor(libre2_value.getCalibratedValue()));

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
