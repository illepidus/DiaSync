package ru.krotarnya.diasync.common.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public enum BloodGlucoseUnit {
    MGDL(BloodGlucose::mgdl, bg -> String.format(Locale.US, "%.0f", bg.mgdl())),
    MMOL(BloodGlucose::mmol, bg -> String.format(Locale.US, "%.1f", bg.mmol()));
    private final Function<BloodGlucose, Double> toValueF;
    private final Function<BloodGlucose, String> toStringF;

    BloodGlucoseUnit(
            Function<BloodGlucose, Double> toValueF,
            Function<BloodGlucose, String> toStringF)
    {
        this.toValueF = toValueF;
        this.toStringF = toStringF;
    }

    public double getValue(BloodGlucose bg) {
        return toValueF.apply(bg);
    }

    public String getString(BloodGlucose bg) {
        return toStringF.apply(bg);
    }

    public static Optional<BloodGlucoseUnit> resolve(String s) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(s))
                .findAny();
    }

    public static BloodGlucoseUnit resolveOrThrow(String s) {
        return resolve(s).orElseThrow(() -> new IllegalArgumentException(s + "is not valid bloodGlucoseUnit"));
    }
}
