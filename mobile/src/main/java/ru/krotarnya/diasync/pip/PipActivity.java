package ru.krotarnya.diasync.pip;

import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ru.krotarnya.diasync.DiasyncDB;
import ru.krotarnya.diasync.DiasyncGraphBuilder;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.common.model.BloodData;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodPoint;
import ru.krotarnya.diasync.common.model.TrendArrow;
import ru.krotarnya.diasync.model.Libre2ValueList;
import ru.krotarnya.diasync.settings.Settings;

public class PipActivity extends AppCompatActivity {
    private static final String TAG = "PipActivity";
    public static final String UPDATE_ACTION = "ru.krotarnya.diasync.activity.PipActivity.update";
    private static final Set<String> UPDATE_ACTIONS = Set.of(Intent.ACTION_TIME_TICK, UPDATE_ACTION);
    private static final Rational ASPECT_RATIO = new Rational(3, 4);
    private static final int PIP_WIDTH = 400;
    private static final int PIP_HEIGHT = PIP_WIDTH * ASPECT_RATIO.getDenominator() / ASPECT_RATIO.getNumerator();
    private ImageView canvas;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UPDATE_ACTIONS.contains(intent.getAction())) {
                Log.d(TAG, "Received " + intent.getAction());
                update();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pip);
        canvas = findViewById(R.id.pip_canvas);
        Optional.ofNullable(getSupportActionBar()).ifPresent(ActionBar::hide);

        IntentFilter intentFilter = new IntentFilter();
        UPDATE_ACTIONS.forEach(intentFilter::addAction);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        update();
        enterPictureInPicture();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        enterPictureInPicture();
    }

    private void enterPictureInPicture() {
        enterPictureInPictureMode(new PictureInPictureParams.Builder()
                                          .setAspectRatio(ASPECT_RATIO)
                                          .setAutoEnterEnabled(true)
                                          .build());
    }

    private void update() {
        BloodData bloodData = getBloodData(this);
        canvas.setImageBitmap(new DiasyncGraphBuilder()
                                      .setData(bloodData)
                                      .setWidth(PIP_WIDTH)
                                      .setHeight(PIP_HEIGHT)
                                      .build());
    }

    private BloodData getBloodData(Context context) {
        //TODO: redo database activity
        Settings settings = Settings.getInstance(context);
        long graph_period = settings.widgetTimeWindow().toMillis();
        long t2 = System.currentTimeMillis(), t1 = t2 - graph_period;

        DiasyncDB diasync_db = DiasyncDB.getInstance(context);
        Libre2ValueList libre2Values = diasync_db.getLibre2Values(t1, t2 + 60000);

        List<BloodPoint> points = libre2Values.stream()
                .map(v -> new BloodPoint(
                        Instant.ofEpochMilli(v.timestamp),
                        BloodGlucose.consMgdl(v.getValue())))
                .collect(Collectors.toList());

        BloodData.Params params = new BloodData.Params(
                settings.glucoseUnit(),
                settings.glucoseLow(),
                settings.glucoseHigh(),
                settings.widgetTimeWindow(),
                new BloodData.Colors(
                        ContextCompat.getColor(context, R.color.widget_background),
                        ContextCompat.getColor(context, R.color.widget_glucose_low),
                        ContextCompat.getColor(context, R.color.widget_glucose_normal),
                        ContextCompat.getColor(context, R.color.widget_glucose_high),
                        ContextCompat.getColor(context, R.color.widget_text_low),
                        ContextCompat.getColor(context, R.color.widget_text_normal),
                        ContextCompat.getColor(context, R.color.widget_text_high),
                        ContextCompat.getColor(context, R.color.widget_text_error),
                        ContextCompat.getColor(context, R.color.widget_zone_low),
                        ContextCompat.getColor(context, R.color.widget_zone_normal),
                        ContextCompat.getColor(context, R.color.widget_zone_high)));

        return new BloodData(points, TrendArrow.of(points), params);
    }
}