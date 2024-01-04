package ru.krotarnya.diasync.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import ru.krotarnya.diasync.common.util.BloodUtils;

@SuppressWarnings("ClassCanBeRecord")
public final class BloodGlucose implements Comparable<BloodGlucose> {
    private final double mgdl;

    public BloodGlucose(@JsonProperty("mgdl") double mgdl) {
        this.mgdl = mgdl;
    }

    public static BloodGlucose consMgdl(double mgdl) {
        return new BloodGlucose(mgdl);
    }

    @SuppressWarnings("unused")
    public static BloodGlucose consMmol(double mmol) {
        return new BloodGlucose(BloodUtils.mmolToMgdl(mmol));
    }

    public double mmol() {
        return BloodUtils.mgdlToMmol(mgdl);
    }

    public double mgdl() {
        return mgdl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BloodGlucose) obj;
        return Double.doubleToLongBits(this.mgdl) == Double.doubleToLongBits(that.mgdl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mgdl);
    }

    @Override
    public String toString() {
        return "BloodGlucose[" +
                "mgdl=" + mgdl + ']';
    }

    @Override
    public int compareTo(BloodGlucose other) {
        return Double.compare(mgdl(), other.mgdl());
    }

    public boolean gt(BloodGlucose other) {
        return (compareTo(other) > 0);
    }

    public boolean eq(BloodGlucose other) {
        return (compareTo(other) == 0);
    }

    public boolean lt(BloodGlucose other) {
        return (compareTo(other) < 0);
    }
}
