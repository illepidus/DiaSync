package ru.krotarnya.diasync.common.model;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public enum TrendArrow {
    UNKNOWN("?", null),
    DOUBLE_DOWN("⇊", null),
    SINGLE_DOWN("↓", -13.5),
    DOWN_45("↘", -7.0),
    FLAT("→", -3.0),
    UP_45("↗", 3.0),
    SINGLE_UP("↑", 7.0),
    DOUBLE_UP("⇈", 13.5);
    private static final Duration TREND_WINDOW_DURATION = Duration.ofMinutes(10);
    private final String symbol;
    private final Double threshold;

    TrendArrow(String symbol, Double threshold) {
        this.symbol = symbol;
        this.threshold = threshold;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public static TrendArrow of(double value) {
        return Arrays.stream(TrendArrow.values())
                .filter(a -> a.threshold != null && value > a.threshold)
                .max(Comparator.comparingDouble(trendArrow -> trendArrow.threshold))
                .orElse(DOUBLE_DOWN);
    }
    
    public static TrendArrow of(List<BloodPoint> points) {
        Optional<BloodPoint> lastPoint = points.stream()
                .max(Comparator.comparingLong(x -> x.time().toEpochMilli()));

        Optional<BloodGlucose> avgBloodGlucose = lastPoint
                .map(last -> last.time().minus(TREND_WINDOW_DURATION))
                .flatMap(deadline -> points.stream()
                        .filter(p -> !p.time().isBefore(deadline))
                        .filter(p -> p.time().isBefore(lastPoint.get().time()))
                        .mapToDouble(p -> p.glucose().mgdl())
                        .average()
                        .stream()
                        .boxed()
                        .findAny()
                        .map(BloodGlucose::consMgdl));

        return avgBloodGlucose.map(avg -> lastPoint.get().glucose().mgdl() - avg.mgdl())
                .map(TrendArrow::of)
                .orElse(UNKNOWN);
    }
}
