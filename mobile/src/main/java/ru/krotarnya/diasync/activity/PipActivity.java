package ru.krotarnya.diasync.activity;

import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.util.Optional;
import java.util.Set;

import ru.krotarnya.diasync.DiasyncDB;
import ru.krotarnya.diasync.Glucose;
import ru.krotarnya.diasync.Libre2GraphBuilder;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.model.Libre2Value;
import ru.krotarnya.diasync.model.Libre2ValueList;

public class PipActivity extends AppCompatActivity {
    private static final String TAG = "PipActivity";
    public static final String UPDATE_ACTION = "ru.krotarnya.diasync.activity.PipActivity.update";
    private static final Set<String> UPDATE_ACTIONS = Set.of(Intent.ACTION_TIME_TICK, UPDATE_ACTION);
    private static final int GRAPH_WIDTH = 400;
    private static final Rational ASPECT_RATIO = new Rational(3, 4);
    private TextView glucose;
    private TextView trend;
    private ImageView graph;

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
        setContentView(R.layout.pip_libre2);
        glucose = findViewById(R.id.libre2_pip_glucose);
        trend = findViewById(R.id.libre2_pip_trend);
        graph = findViewById(R.id.libre2_pip_graph);
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
        PictureInPictureParams.Builder paramsBuilder = new PictureInPictureParams.Builder()
                .setAspectRatio(ASPECT_RATIO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            paramsBuilder = paramsBuilder.setAutoEnterEnabled(true);
        }

        enterPictureInPictureMode(paramsBuilder.build());
    }

    private void update() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String glucose_units = prefs.getString("glucose_units", "mmol");
        boolean graph_range_lines = prefs.getBoolean("libre2_widget_graph_range_lines", false);
        boolean graph_range_zones = prefs.getBoolean("libre2_widget_graph_range_zones", true);
        long graph_period = Long.parseLong(prefs.getString("libre2_widget_graph_period", "1800000"));

        long t2 = System.currentTimeMillis(), t1 = t2 - graph_period;
        DiasyncDB diasync_db = DiasyncDB.getInstance(this);
        Libre2ValueList libre2_values = diasync_db.getLibre2Values(t1, t2 + 60000);

        if ((libre2_values == null) || (libre2_values.size() == 0)) {
            glucose.setText("----");
            trend.setText("-");
            graph.setImageResource(android.R.color.transparent);
        } else {
            Libre2Value libre2_last_value = libre2_values.maxByTimestamp();

            switch (glucose_units) {
                case "mmol":
                        glucose.setText(Glucose.stringMmol(libre2_last_value.getMmolValue()));
                        break;
                case "mgdl":
                        glucose.setText(Glucose.stringMgdl(libre2_last_value.getMmolValue()));
                        break;
                default:
                    glucose.setText("----");
                    break;
            }

            glucose.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
            trend.setText(libre2_values.getTrendArrow().getSymbol());

            try {
                graph.setImageBitmap(new Libre2GraphBuilder(this)
                        .setWidth(GRAPH_WIDTH)
                        .setHeight((float) GRAPH_WIDTH * ASPECT_RATIO.getDenominator() / ASPECT_RATIO.getNumerator())
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
            glucose.setTextColor(Glucose.bloodTextColor(libre2_last_value.getValue()));
        }
    }
}