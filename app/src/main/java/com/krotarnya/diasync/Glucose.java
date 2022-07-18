package com.krotarnya.diasync;

import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.text.ParseException;

public abstract class Glucose {
    static double mgdlToMmol(double v) {
        return v * 0.0555;
    }
    static double mgdlToMmol(String v) {
        return mgdlToMmol(glucose(v));
    }
    static double mmolToMgdl(double v) {
        return v * 18;
    }
    static double mmolToMgdl(String v) {
        return mmolToMgdl(glucose(v));
    }

    static boolean isHigh(double v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Diasync.getContext());
        float high_value_mmol = prefs.getFloat("high_value_mmol", 180.f);
        return (v > high_value_mmol);
    }

    static boolean isLow(double v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Diasync.getContext());
        float low_value_mmol = prefs.getFloat("low_value_mmol", 70.f);
        return (v < low_value_mmol);
    }

    static boolean isNormal(double v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Diasync.getContext());
        float low_value_mmol = prefs.getFloat("low_value_mmol", 70.f);
        float high_value_mmol = prefs.getFloat("high_value_mmol", 180.f);
        return ((v >= low_value_mmol) && (v <= high_value_mmol));
    }

    static int bloodTextColor(double v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Diasync.getContext());
        float low_value_mmol = prefs.getFloat("low_value_mmol", 70.f);
        float high_value_mmol = prefs.getFloat("high_value_mmol", 180.f);

        if (v <= 0)
            return ContextCompat.getColor(Diasync.getContext(), R.color.blood_error_text);
        if (v <  low_value_mmol)
            return ContextCompat.getColor(Diasync.getContext(), R.color.blood_low_text);
        if (v <  high_value_mmol)
            return ContextCompat.getColor(Diasync.getContext(), R.color.blood_normal_text);
        return ContextCompat.getColor(Diasync.getContext(), R.color.blood_high_text);
    }

    static int bloodGraphColor(double v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Diasync.getContext());
        float low_value_mmol = prefs.getFloat("low_value_mmol", 70.f);
        float high_value_mmol = prefs.getFloat("high_value_mmol", 180.f);

        if (v <= 0)
            return ContextCompat.getColor(Diasync.getContext(), R.color.blood_error_graph);
        if (v <  low_value_mmol)
            return ContextCompat.getColor(Diasync.getContext(), R.color.blood_low_graph);
        if (v <  high_value_mmol)
            return ContextCompat.getColor(Diasync.getContext(), R.color.blood_normal_graph);
        return ContextCompat.getColor(Diasync.getContext(), R.color.blood_high_graph);
    }

    static double glucose(String v) {
        DecimalFormat format = new DecimalFormat();
        if (v == null) return 0;
        try {
            Number n = format.parse(v);
            return (n == null) ? 0 : n.doubleValue();
        } catch (ParseException e) {
            return 0;
        }
    }

    static String stringMmol(double v) {
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(v);
    }

    static String stringMgdl(double v) {
        DecimalFormat format = new DecimalFormat("0");
        return format.format(v);
    }
}
