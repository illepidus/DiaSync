package ru.krotarnya.diasync;

import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Libre2ValueList {
    private static final String TAG = "Libre2ValueList";
    private final List<Libre2Value> values;

    Libre2ValueList(List<Libre2Value> v) {
        values = v;
    }

    public int size() {
        return values.size();
    }

    public Libre2Value get(int i) { return values.get(i); }

    public TrendArrow trendArrow(boolean use_calibration) {
        Libre2Value lastValue = maxTimestamp();
        if (lastValue == null || lastValue.getValue(use_calibration) == 0) return TrendArrow.NONE;
        double last_value = lastValue.getValue(use_calibration);
        long last_timestamp = lastValue.timestamp;
        double sum = 0;
        int count = 0;
        for (Libre2Value value : values) {
            if (last_timestamp - value.timestamp < 600000 && last_timestamp != value.timestamp) {
                sum += value.getValue(use_calibration);
                count++;
            }
        }
        if (sum <= 0 || count <= 0) return TrendArrow.NONE;
        Log.d(TAG, "Delta = " + (last_value - sum/count) + "; TREND = " + TrendArrow.getTrend(last_value - sum/count));
        return TrendArrow.getTrend(last_value - sum/count);
    }

    public String trendArrowSymbol(boolean use_calibration) { return trendArrow(use_calibration).getSymbol(); }

    public Libre2Value maxValue(boolean use_calibration) {
        return Collections.max(values, new Libre2ValueComp(use_calibration));
    }

    public Libre2Value minValue(boolean use_calibration) {
        return Collections.min(values, new Libre2ValueComp(use_calibration));
    }

    public Libre2Value maxTimestamp() {
        return Collections.max(values, new Libre2TimestampComp());
    }

    static class Libre2ValueComp implements Comparator<Libre2Value> {
        final boolean use_calibration;

        public Libre2ValueComp(boolean use_calibration){
            this.use_calibration = use_calibration;
        }

        @Override
        public int compare(Libre2Value v1, Libre2Value v2) {
            return Double.compare(v1.getValue(use_calibration), v2.getValue(use_calibration));
        }
    }

    static class Libre2TimestampComp implements Comparator<Libre2Value> {
        @Override
        public int compare(Libre2Value v1, Libre2Value v2) {
            return Long.compare(v1.timestamp, v2.timestamp);
        }
    }
}

