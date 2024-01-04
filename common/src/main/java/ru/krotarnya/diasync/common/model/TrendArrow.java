package ru.krotarnya.diasync.common.model;

import java.util.Arrays;
import java.util.Comparator;

public enum TrendArrow {
    NONE(" ", null),
    DOUBLE_DOWN("⇊", null),
    SINGLE_DOWN("↓", -13.5),
    DOWN_45("↘", -7.0),
    FLAT("→", -3.0),
    UP_45("↗", 3.0),
    SINGLE_UP("↑", 7.0),
    DOUBLE_UP("⇈", 13.5);

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
}
