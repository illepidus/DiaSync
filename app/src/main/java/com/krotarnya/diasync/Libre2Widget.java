package com.krotarnya.diasync;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DecimalFormat;
import java.util.Objects;

public class Libre2Widget extends AppWidgetProvider {
    private static final String TAG = "Libre2Widget";
    private static final String WIDGET_CLICKED_TAG = "WIDGET_CLICKED";
    private static final DecimalFormat mmol_format = new DecimalFormat("0.0");

    @SuppressLint("ResourceType")
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_libre2);

        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2Value libre2_value = diasync_db.getLastLibre2Value();
        views.setTextViewText(R.id.blood_glucose, mmol_format.format(libre2_value.getCalibratedMmolValue()));
        views.setTextColor(R.id.blood_glucose, Color.parseColor(context.getString(R.color.blood_normal)));
        views.setOnClickPendingIntent(R.id.root_layout, getPendingSelfIntent(context, WIDGET_CLICKED_TAG));

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
