package ru.krotarnya.diasync.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Arrays;

import ru.krotarnya.diasync.R;

public final class DiasyncWidget extends AppWidgetProvider {
    private static final String TAG = DiasyncWidget.class.getSimpleName();

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
        Log.d(TAG, intent.toString());
        Toast.makeText(context, intent.toString(), Toast.LENGTH_SHORT).show();
    }

    private void update(Context context, AppWidgetManager appWidgetManager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diasync_widget);
        update(context, views);
        appWidgetManager.updateAppWidget(id, views);
    }

    private void update(Context context, RemoteViews views) {
        views.setImageViewResource(R.id.diasync_widget_canvas, android.R.color.holo_red_dark);
        Intent intent = new Intent(context, DiasyncWidget.class).setAction("ACTION_TEST");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.diasync_widget_canvas, pendingIntent);
    }
}
