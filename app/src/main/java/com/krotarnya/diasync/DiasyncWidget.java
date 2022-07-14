package com.krotarnya.diasync;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import java.text.DecimalFormat;

public class DiasyncWidget extends AppWidgetProvider {
    private static final String TAG = "DiasyncWidget";
    private static final DecimalFormat mmol_format = new DecimalFormat("0.0");

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diasync_widget);

        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2Value libre2_value = diasync_db.getLastLibre2Value();
        views.setTextViewText(R.id.appwidget_text, mmol_format.format(libre2_value.getCalibratedMmolValue()));

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

    /*
    Не похоже, что нужно обновление через непосредственную переадачу данных, оставлю на всякий случай
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action) && intent.hasExtra("libre2_bg")) {
            //libre2_value = Libre2Value.fromBundle(intent.getBundleExtra("libre2_bg"));
        }
    }
    */
}
