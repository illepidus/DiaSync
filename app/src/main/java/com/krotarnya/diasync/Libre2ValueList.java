package com.krotarnya.diasync;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Libre2ValueList {
    private final List<Libre2Value> values;

    Libre2ValueList(List<Libre2Value> v) {
        values = v;
    }

    public int size() {
        return values.size();
    }

    public Libre2Value get(int i) { return values.get(i); }

    public Libre2Value maxCalibratedValue() {
        return Collections.max(values, new Libre2CalibratedValueComp());
    }

    public Libre2Value minCalibratedValue() {
        return Collections.min(values, new Libre2CalibratedValueComp());
    }

    public Libre2Value maxValue() {
        return Collections.max(values, new Libre2ValueComp());
    }

    public Libre2Value minValue() {
        return Collections.min(values, new Libre2ValueComp());
    }

    public Libre2Value minTimestamp() {
        return Collections.min(values, new Libre2TimestampComp());
    }

    public Libre2Value maxTimestamp() {
        return Collections.max(values, new Libre2TimestampComp());
    }

    static class Libre2CalibratedValueComp implements Comparator<Libre2Value> {
        @Override
        public int compare(Libre2Value v1, Libre2Value v2) {
            return Double.compare(v1.value, v2.value);
        }
    }

    static class Libre2ValueComp implements Comparator<Libre2Value> {
        @Override
        public int compare(Libre2Value v1, Libre2Value v2) {
            return Double.compare(v1.getCalibratedValue(), v2.getCalibratedValue());
        }
    }

    static class Libre2TimestampComp implements Comparator<Libre2Value> {
        @Override
        public int compare(Libre2Value v1, Libre2Value v2) {
            return Long.compare(v1.timestamp, v2.timestamp);
        }
    }
}
