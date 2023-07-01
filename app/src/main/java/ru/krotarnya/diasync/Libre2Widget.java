package ru.krotarnya.diasync;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import ru.krotarnya.diasync.model.Libre2Value;
import ru.krotarnya.diasync.model.Libre2ValueList;

public class Libre2Widget extends AppWidgetProvider {
    private static final String TAG = "Libre2Widget";
    private static final String WIDGET_CLICKED_TAG = "ru.krotarnya.diasync.WIDGET_CLICKED";
    public static final String WIDGET_ALERTS_ICON_CLICKED_TAG = "ru.krotarnya.diasync.WIDGET_ALERTS_ICON_CLICKED";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                    int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_libre2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String glucose_units = prefs.getString("glucose_units", "mmol");
        boolean graph_enabled = prefs.getBoolean("libre2_widget_graph_enabled", true);
        boolean graph_range_lines = prefs.getBoolean("libre2_widget_graph_range_lines", false);
        boolean graph_range_zones = prefs.getBoolean("libre2_widget_graph_range_zones", true);
        boolean alerts_icon = prefs.getBoolean("libre2_widget_alerts_icon", true);
        long graph_period = Long.parseLong(prefs.getString("libre2_widget_graph_period", "1800000"));

        if (alerts_icon)
            views.setImageViewResource(R.id.libre2_widget_alerts_icon, R.drawable.ic_bell_gear);
        else
            views.setImageViewResource(R.id.libre2_widget_alerts_icon, android.R.color.transparent);

        long t2 = System.currentTimeMillis(), t1 = t2 - graph_period;
        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2ValueList libre2_values = diasync_db.getLibre2Values(t1, t2 + 60000);

        if ((libre2_values == null) || (libre2_values.size() == 0)) {
            views.setTextViewText(R.id.libre2_widget_glucose, "----");
            views.setTextViewText(R.id.libre2_widget_trend, "-");
            views.setTextViewText(R.id.Libre2_widget_message, "NO DATA");
            views.setImageViewResource(R.id.libre2_widget_graph, android.R.color.transparent);
        }
        else {
            Libre2Value libre2_last_value = libre2_values.maxByTimestamp();
            long ago = (t2 - libre2_last_value.timestamp);

            switch (glucose_units) {
                case "mmol":
                        views.setTextViewText(R.id.libre2_widget_glucose, Glucose.stringMmol(libre2_last_value.getMmolValue()));
                    break;
                case "mgdl":
                        views.setTextViewText(R.id.libre2_widget_glucose, Glucose.stringMgdl(libre2_last_value.getValue()));
                    break;
                default:
                    Log.wtf(TAG, "Unknown glucose units");
                    views.setTextViewText(R.id.libre2_widget_glucose, "----");
            }

            views.setInt(R.id.libre2_widget_glucose, "setPaintFlags", Paint.ANTI_ALIAS_FLAG);
            views.setTextViewText(R.id.libre2_widget_trend, libre2_values.getTrendArrow().getSymbol());
            if (ago < - 60000) {
                //DATA FROM FAR FUTURE
                Log.w(TAG, "Received data from far future. Don't know how to display it");
                views.setTextViewText(R.id.libre2_widget_glucose, "----");
                views.setTextViewText(R.id.Libre2_widget_message, "DATA FROM FAR FUTURE");
                views.setImageViewResource(R.id.libre2_widget_graph, android.R.color.transparent);
            } else if (ago < 60000) {
                //FRESH
                views.setTextViewText(R.id.Libre2_widget_message, "");
            } else {
                //DATA IS OLD
                if (ago / 60000 == 1)
                    views.setTextViewText(R.id.Libre2_widget_message, "1 minute ago");
                else
                    views.setTextViewText(R.id.Libre2_widget_message, ago / 60000 + " minutes ago");
                if (ago > 600000) {
                    views.setInt(R.id.libre2_widget_glucose, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                }
            }

            if (graph_enabled) {
                int width =  appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
                int height = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
                if (width > 0 && height > 0) {
                    try {
                        views.setImageViewBitmap(R.id.libre2_widget_graph, new Libre2GraphBuilder(context)
                            .setWidth(width)
                            .setHeight(height)
                            .setXMin(t1 - 60000)
                            .setXMax(t2 + 60000)
                            .setYMin(Double.min((libre2_values.minByValue().getValue()), Glucose.low()) - 18)
                            .setYMax(Double.max((libre2_values.maxByValue().getValue()), Glucose.high()) + 18)
                            .setRangeLines(graph_range_lines)
                            .setRangeZones(graph_range_zones)
                            .setData(libre2_values)
                            .build());
                    } catch (Exception e) {
                        Log.e(TAG, "Wasn't able to build Libre2Graph");
                    }
                }
                else {
                    views.setImageViewResource(R.id.libre2_widget_graph, android.R.color.transparent);
                }
            }
            else {
                views.setImageViewResource(R.id.libre2_widget_graph, android.R.color.transparent);
            }
            views.setTextColor(R.id.libre2_widget_glucose, Glucose.bloodTextColor(libre2_last_value.getValue()));
        }

        views.setOnClickPendingIntent(R.id.libre2_widget_layout, getPendingSelfIntent(context, WIDGET_CLICKED_TAG));
        views.setOnClickPendingIntent(R.id.libre2_widget_alerts_icon, getPendingSelfIntent(context, WIDGET_ALERTS_ICON_CLICKED_TAG));
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
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        updateAppWidget(context, appWidgetManager, appWidgetId);
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
            WidgetUpdateService.pleaseUpdate(context);
            Alerter.check();
            switch (on_click) {
                case "update":
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
        if (Objects.equals(action, WIDGET_ALERTS_ICON_CLICKED_TAG)) {
            Intent alarms_intent = new Intent(Intent.ACTION_VIEW);
            alarms_intent.setClassName(context.getPackageName(), DiasyncSettings.class.getName());
            alarms_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alarms_intent.putExtra("fragment", DiasyncSettings.ALERTS_FRAGMENT);
            context.startActivity(alarms_intent);
        }
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, Libre2Widget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
