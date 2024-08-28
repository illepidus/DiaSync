package ru.krotarnya.diasync.settings;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodGlucoseUnit;

public enum DynamicSetting {
    GLUCOSE_LOW("glucose_low", "70.0", BloodGlucose::consMgdl),
    GLUCOSE_HIGH("glucose_high", "180.0", BloodGlucose::consMgdl),
    GLUCOSE_UNIT("glucose_unit", "mmol", BloodGlucoseUnit::resolveOrThrow),
    USE_CALIBRATIONS("use_calibrations", "true", Boolean::parseBoolean),
    WIDGET_TIME_WINDOW("widget_time_window", "1800000", s -> Duration.ofMillis(Long.parseLong(s))),
    ;

    private final String key;
    private final String defaultValue;
    private final Function<String, ?> extractor;

    public static Stream<DynamicSetting> stream() {
        return Arrays.stream(values());
    }

    private static final Map<String, DynamicSetting> MAP = stream()
            .collect(Collectors.toMap(DynamicSetting::key, Function.identity()));

    DynamicSetting(String key, String defaultValue, Function<String, ?> extractor) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.extractor = extractor;
    }

    public String key() {
        return key;
    }

    public Object get(SharedPreferences preferences) {
        return extractor.apply(preferences.getString(key, defaultValue));
    }

    public static Optional<DynamicSetting> resolve(@Nullable String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(MAP.get(key));
    }
}
