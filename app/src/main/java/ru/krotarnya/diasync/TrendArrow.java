package ru.krotarnya.diasync;

import java.util.Arrays;
import java.util.Comparator;

public enum TrendArrow {
    NONE("\u0020", null),
    DOUBLE_DOWN("\u21CA", null),
    SINGLE_DOWN("\u2193", -13.5),
    DOWN_45("\u2198", -7.0),
    FLAT("\u2192", -3.0),
    UP_45("\u2197", 3.0),
    SINGLE_UP("\u2191", 7.0),
    DOUBLE_UP("\u21C8", 13.5);

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
