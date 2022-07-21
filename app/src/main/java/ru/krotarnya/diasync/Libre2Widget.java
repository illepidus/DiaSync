package ru.krotarnya.diasync;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.preference.PreferenceManager;

import ru.krotarnya.diasync.R;

import java.util.Objects;

public class Libre2Widget extends AppWidgetProvider {
    private static final String TAG = "Libre2Widget";
    private static final String WIDGET_CLICKED_TAG = "WIDGET_CLICKED";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                    int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_libre2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String glucose_units = prefs.getString("glucose_units", "mmol");
        boolean graph_enabled = prefs.getBoolean("libre2_widget_graph_enabled", true);
        boolean graph_range_lines = prefs.getBoolean("libre2_widget_graph_range_lines", false);
        long graph_period = Long.parseLong(prefs.getString("libre2_widget_graph_period", "1800000"));

        long t2 = System.currentTimeMillis(), t1 = t2 - graph_period;
        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2ValueList libre2_values = diasync_db.getLibre2Values(t1, t2 + 60000);

        if ((libre2_values == null) || (libre2_values.size() == 0)) {
            views.setTextViewText(R.id.libre2_widget_glucose, "----");
            views.setTextViewText(R.id.libre2_widget_glucose, "----");
            views.setImageViewResource(R.id.libre2_widget_graph, android.R.color.transparent);
        }
        else {
            Libre2Value libre2_last_value = libre2_values.maxTimestamp();
            if (graph_enabled)
                views.setImageViewBitmap(R.id.libre2_widget_graph, new Libre2GraphBuilder(context)
                    .setWidth(appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH))
                    .setHeight(appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT))
                    .setXMin(t1 - 120000)
                    .setXMax(t2 + 120000)
                    .setYMin(Double.min((libre2_values.minCalibratedValue().getCalibratedValue()), Glucose.low()) - 18)
                    .setYMax(Double.max((libre2_values.maxCalibratedValue().getCalibratedValue()), Glucose.high()) + 18)
                    .setRangeLines(graph_range_lines)
                    .setData(libre2_values)
                    .build());
            else
                views.setImageViewResource(R.id.libre2_widget_graph, android.R.color.transparent);

            switch (glucose_units) {
                case "mmol":
                    views.setTextViewText(R.id.libre2_widget_glucose, Glucose.stringMmol(libre2_last_value.getCalibratedMmolValue()));
                    break;
                case "mgdl":
                    views.setTextViewText(R.id.libre2_widget_glucose, Glucose.stringMgdl(libre2_last_value.getCalibratedValue()));
                    break;
                default:
                    Log.wtf(TAG, "Unknown glucose units");
                    views.setTextViewText(R.id.libre2_widget_glucose, "----");
            }

            views.setTextViewText(R.id.data_timer, (t2 - libre2_last_value.timestamp) / 1000 + " seconds ago");
            views.setTextColor(R.id.libre2_widget_glucose, Glucose.bloodTextColor(libre2_last_value.getCalibratedValue()));
        }

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
        Log.d(TAG, "Received new intent action [" + action + "] in context [" + context + "]");
        if (Objects.equals(action, WIDGET_CLICKED_TAG)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String on_click = prefs.getString("libre2_widget_on_click", "settings");
            Log.d(TAG, "Widget clicked. Action = " + on_click);
            switch (on_click) {
                case "update":
                    WidgetUpdateService.pleaseStart(context);
                    break;
                case "settings":
                    Intent settingsIntent = new Intent(Intent.ACTION_VIEW);
                    settingsIntent.setClassName(context.getPackageName(), DiasyncSettings.class.getName());
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(settingsIntent);
                    break;
                case "xdrip":
                    Intent xdripIntent = new Intent(Intent.ACTION_VIEW);
                    xdripIntent.setClassName("com.eveningoutpost.dexdrip", "com.eveningoutpost.dexdrip.Home");
                    xdripIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(xdripIntent);
                    break;
            }
        }
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, Libre2Widget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
