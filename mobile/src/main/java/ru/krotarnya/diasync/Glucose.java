package ru.krotarnya.diasync;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;
import java.util.Optional;

public class Glucose implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "Glucose";
    private static final double MMOL_TO_MGDL = 18.0182d;
    private static final double MGDL_TO_MMOL = 1 / MMOL_TO_MGDL;
    private static Glucose INSTANCE;
    private final int widget_background_color;
    private final int error_text_color;
    private final int error_graph_color;
    private final int low_text_color;
    private final int low_graph_color;
    private final int low_graph_zone_color;
    private final int normal_text_color;
    private final int normal_graph_color;
    private final int normal_graph_zone_color;
    private final int high_text_color;
    private final int high_graph_color;
    private final int high_graph_zone_color;
    private volatile double low;
    private volatile double high;
    private volatile boolean useCalibrations;

    private final DecimalFormat mmol_format;
    private final DecimalFormat mgdl_format;

    public Glucose() {
        Context context = Diasync.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        DecimalFormatSymbols decimal_format_symbols = new DecimalFormatSymbols(Locale.US);
        mmol_format = new DecimalFormat("0.0", decimal_format_symbols);
        mgdl_format = new DecimalFormat("0", decimal_format_symbols);

        widget_background_color = ContextCompat.getColor(context, R.color.widget_background);
        error_text_color = ContextCompat.getColor(context, R.color.glucose_error_text);
        error_graph_color = ContextCompat.getColor(context, R.color.glucose_error_graph);
        low_text_color = ContextCompat.getColor(context, R.color.glucose_low_text);
        low_graph_color = ContextCompat.getColor(context, R.color.glucose_low_graph);
        low_graph_zone_color = ContextCompat.getColor(context, R.color.glucose_low_graph_zone);
        normal_text_color = ContextCompat.getColor(context, R.color.glucose_normal_text);
        normal_graph_color = ContextCompat.getColor(context, R.color.glucose_normal_graph);
        normal_graph_zone_color = ContextCompat.getColor(context, R.color.glucose_normal_graph_zone);
        high_text_color = ContextCompat.getColor(context, R.color.glucose_high_text);
        high_graph_color = ContextCompat.getColor(context, R.color.glucose_high_graph);
        high_graph_zone_color = ContextCompat.getColor(context, R.color.glucose_high_graph_zone);
        low = Double.parseDouble(prefs.getString("glucose_low", "70"));
        high = Double.parseDouble(prefs.getString("glucose_high", "180"));
        useCalibrations = prefs.getBoolean("use_calibrations", true);

        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public static synchronized Glucose getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Glucose();
        }
        return INSTANCE;
    }

    public static double mgdlToMmol(double v) {
        return v * MGDL_TO_MMOL;
    }

    public static double mgdlToMmol(String v) {
        return mgdlToMmol(parse(v));
    }

    public static double mmolToMgdl(double v) {
        return v * 18.0182d;
    }

    public static double mmolToMgdl(String v) {
        return mmolToMgdl(parse(v));
    }

    public static int widgetBackgroundColor() {
        return getInstance().widget_background_color;
    }

    public static int errorTextColor() {
        return getInstance().error_text_color;
    }

    public static int lowTextColor() {
        return getInstance().low_text_color;
    }

    public static int lowGraphColor() {
        return getInstance().low_graph_color;
    }

    public static int lowGraphZoneColor() {
        return getInstance().low_graph_zone_color;
    }

    public static int normalTextColor() {
        return getInstance().normal_text_color;
    }

    public static int normalGraphColor() {
        return getInstance().normal_graph_color;
    }

    public static int normalGraphZoneColor() {
        return getInstance().normal_graph_zone_color;
    }

    public static int highTextColor() {
        return getInstance().high_text_color;
    }

    public static int highGraphColor() {
        return getInstance().high_graph_color;
    }

    public static int highGraphZoneColor() {
        return getInstance().high_graph_zone_color;
    }

    public static double high() {
        return getInstance().high;
    }

    public static double low() {
        return getInstance().low;
    }

    public static int bloodTextColor(double v) {
        if (v <= 0) return errorTextColor();
        if (v < low()) return lowTextColor();
        if (v < high()) return normalTextColor();
        return highTextColor();
    }

    public static int bloodGraphColor(double v) {
        if (v <= 0) return getInstance().error_graph_color;
        if (v < low()) return lowGraphColor();
        if (v < high()) return normalGraphColor();
        return highGraphColor();
    }

    public static double parse(String v) {
        if (v == null) return 0;
        try {
            return Double.parseDouble(v);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing [" + v + "] to double");
            return 0;
        }
    }

    public static String stringMmol(double v) {
        return getInstance().mmol_format.format(v);
    }

    public static String stringMgdl(double v) {
        return getInstance().mgdl_format.format(v);
    }

    public static boolean useCalibrations() {
        return getInstance().useCalibrations;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String key) {
        switch (Optional.ofNullable(key).orElse("")) {
            case "glucose_low":
                low = Double.parseDouble(p.getString("glucose_low", "70"));
                Log.v(TAG, "glucose_low = " + low);
                break;
            case "glucose_high":
                high = Double.parseDouble(p.getString("glucose_high", "180"));
                Log.v(TAG, "glucose_high = " + high);
                break;
            case "use_calibrations":
                useCalibrations = p.getBoolean("use_calibrations", true);
                Log.v(TAG, "use_calibrations = " + useCalibrations);
                break;
        }
    }
}
