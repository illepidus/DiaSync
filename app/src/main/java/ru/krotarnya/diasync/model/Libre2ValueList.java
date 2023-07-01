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

    public TrendArrow getTrendArrow() {
        return Optional.ofNullable(maxByTimestamp())
                .flatMap(max -> {
                    OptionalDouble avg = this.stream()
                            .filter(v -> v.timestamp >= max.timestamp - TREND_WINDOW_MILLIS
                                    && v.timestamp < max.timestamp)
                            .mapToDouble(Libre2Value::getValue)
                            .average();

                    //Stupid Java 11 without OptionalDouble.stream()
                    return Optional.ofNullable(avg.isPresent()
                            ? TrendArrow.of(max.getValue() - avg.getAsDouble())
                            : null);
                })
                .orElse(TrendArrow.NONE);
    }

    public Libre2Value maxByValue() {
        return Collections.max(this, Comparator.comparingDouble(Libre2Value::getValue));
    }

    public Libre2Value minByValue() {
        return Collections.min(this, Comparator.comparingDouble(Libre2Value::getValue));
    }

    public Libre2Value maxByTimestamp() {
        return Collections.max(this, Comparator.comparingLong(v -> v.timestamp));
    }
}