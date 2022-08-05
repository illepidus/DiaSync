package ru.krotarnya.diasync;

public class DiasyncConstants {
    public final static float MG_DL_TO_MMOL_L = 0.05556f;

    public enum TREND_ARROW_VALUES {
        NONE(0, "", "None", null),
        DOUBLE_UP(1, "\u21C8", "DoubleUp", 40d),
        SINGLE_UP(2, "\u2191", "SingleUp", 13.5d),
        UP_45(3, "\u2197", "FortyFiveUp", 7d),
        FLAT(4, "\u2192", "Flat", 3d),
        DOWN_45(5, "\u2198", "FortyFiveDown", -3d),
        SINGLE_DOWN(6, "\u2193", "SingleDown", -7d),
        DOUBLE_DOWN(7, "\u21CA", "DoubleDown", -13.5d),
        NOT_COMPUTABLE(8, "", "NotComputable", null),
        OUT_OF_RANGE(9, "", "RateOutOfRange", null);

        private final String symbol;
        private final String name;
        private final Double threshold;
        private final int id;

        TREND_ARROW_VALUES(int id, String symbol, String name, Double threshold) {
            this.id = id;
            this.symbol = symbol;
            this.name = name;
            this.threshold = threshold;
        }

        public String arrowSymbol() {
            return this.symbol;
        }
        public String trendName() {
            return this.name;
        }

        public static TREND_ARROW_VALUES getTrend(double value) {
            TREND_ARROW_VALUES finalTrend = NONE;
            for (TREND_ARROW_VALUES trend : values()) {
                if (trend.threshold == null)
                    continue;
                if (value > trend.threshold)
                    return finalTrend;
                else
                    finalTrend = trend;
            }
            return finalTrend;
        }
    }
}
