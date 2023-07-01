package ru.krotarnya.diasync.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalDouble;

public class Libre2ValueList extends ArrayList<Libre2Value> {
    private static final long TREND_WINDOW_MILLIS = 600_000;

    public Libre2ValueList() {
        super();
    }

    public TrendArrow getTrendArrow(boolean useCalibration) {
        return Optional.ofNullable(maxByTimestamp())
                .flatMap(max -> {
                    OptionalDouble avg = this.stream()
                            .filter(v -> v.timestamp >= max.timestamp - TREND_WINDOW_MILLIS
                                    && v.timestamp < max.timestamp)
                            .mapToDouble(v -> v.getValue(useCalibration))
                            .average();

                    //Stupid Java 11 without OptionalDouble.stream()
                    return Optional.ofNullable(avg.isPresent()
                            ? TrendArrow.of(max.getValue(useCalibration) - avg.getAsDouble())
                            : null);
                })
                .orElse(TrendArrow.NONE);
    }

    public Libre2Value maxByValue(boolean useCalibration) {
        return Collections.max(this, Comparator.comparingDouble(v -> v.getValue(useCalibration)));
    }

    public Libre2Value minByValue(boolean useCalibration) {
        return Collections.min(this, Comparator.comparingDouble(v -> v.getValue(useCalibration)));
    }

    public Libre2Value maxByTimestamp() {
        return Collections.max(this, Comparator.comparingLong(v -> v.timestamp));
    }
}