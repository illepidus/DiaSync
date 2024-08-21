package ru.krotarnya.diasync.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Map;
import java.util.WeakHashMap;

import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodGlucoseUnit;

public class Settings {
    private static final Map<Context, Settings> instances = new WeakHashMap<>();
    private final SharedPreferences preferences;

    private Settings(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @JsonGetter("glucose_low")
    public BloodGlucose glucoseLow() {
        return getDynamicSetting(DynamicSetting.GLUCOSE_LOW, BloodGlucose.class);
    }

    @JsonGetter("glucose_high")
    public BloodGlucose glucoseHigh() {
        return getDynamicSetting(DynamicSetting.GLUCOSE_HIGH, BloodGlucose.class);
    }

    @JsonGetter("glucose_unit")
    public BloodGlucoseUnit glucoseUnit() {
        return getDynamicSetting(DynamicSetting.GLUCOSE_UNIT, BloodGlucoseUnit.class);
    }

    private <T> T getDynamicSetting(DynamicSetting setting, Class<T> clazz) {
        return clazz.cast(setting.get(preferences));
    }

    public static synchronized Settings getInstance(Context context) {
        return instances.computeIfAbsent(context, Settings::new);
    }
}
