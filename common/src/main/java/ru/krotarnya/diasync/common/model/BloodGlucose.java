package ru.krotarnya.diasync.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import ru.krotarnya.diasync.common.DefaultObject;
import ru.krotarnya.diasync.common.util.BloodUtil;

public final class BloodGlucose extends DefaultObject implements Comparable<BloodGlucose> {
    private final double mgdl;

    public BloodGlucose(@JsonProperty("mgdl") double mgdl) {
        this.mgdl = mgdl;
    }

    public static BloodGlucose consMgdl(double mgdl) {
        return new BloodGlucose(mgdl);
    }

    public static BloodGlucose consMgdl(String mgdl) {
        return new BloodGlucose(Double.parseDouble(mgdl));
    }

    public static BloodGlucose consMmol(double mmol) {
        return new BloodGlucose(BloodUtil.mmolToMgdl(mmol));
    }

    public double mmol() {
        return BloodUtil.mgdlToMmol(mgdl);
    }

    public double mgdl() {
        return mgdl;
    }

    @Override
    public int compareTo(BloodGlucose other) {
        return Double.compare(mgdl(), other.mgdl());
    }

    public boolean gt(BloodGlucose other) {
        return (compareTo(other) > 0);
    }

    public boolean lt(BloodGlucose other) {
        return (compareTo(other) < 0);
    }
}
