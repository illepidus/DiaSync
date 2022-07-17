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

        int height = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        int width = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        Libre2GraphBuilder libre2_graph_builder = new Libre2GraphBuilder(context);
        libre2_graph_builder.setWidth(width);
        libre2_graph_builder.setHeight(height);
        views.setImageViewBitmap(R.id.libre2_widget_graph, libre2_graph_builder.build());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String glucose_units = prefs.getString("glucose_units", "mmol");
        double glucose_low  = Glucose.glucose(prefs.getString("glucose_low" , "3.9" ));
        double glucose_high = Glucose.glucose(prefs.getString("glucose_high", "10.0"));

        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2Value libre2_value = diasync_db.getLastLibre2Value();

        String blood_color = context.getString(R.color.blood_error);
        double value;
        switch (glucose_units) {
            case "mmol":
                value = libre2_value.getCalibratedMmolValue();
                views.setTextViewText(R.id.blood_glucose, Glucose.stringMmol(value));
                blood_color = context.getString(R.color.blood_low);
                if (value > glucose_low)  blood_color = context.getString(R.color.blood_normal);
                if (value > glucose_high) blood_color = context.getString(R.color.blood_high);
                break;
            case "mgdl":
                value = libre2_value.getCalibratedValue();
                views.setTextViewText(R.id.blood_glucose, Glucose.stringMgdl(value));
                blood_color = context.getString(R.color.blood_low);
                if (value > glucose_low)  blood_color = context.getString(R.color.blood_normal);
                if (value > glucose_high) blood_color = context.getString(R.color.blood_high);
                break;
            default:
                Log.wtf(TAG, "Unknown glucose units");
                views.setTextViewText(R.id.blood_glucose, "----");
        }
        Date date = new Date(libre2_value.timestamp);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat date_format = new SimpleDateFormat("HH:mm");
        views.setTextViewText(R.id.data_timer, String.valueOf(date_format.format(libre2_value.timestamp)));
        views.setTextColor(R.id.blood_glucose, Color.parseColor(blood_color));

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
