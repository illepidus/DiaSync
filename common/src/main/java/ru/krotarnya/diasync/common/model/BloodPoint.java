package ru.krotarnya.diasync.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class BloodPoint {
    private final Instant time;
    private final BloodGlucose glucose;

    public BloodPoint(
            @JsonProperty("time") Instant time,
            @JsonProperty("glucose") BloodGlucose glucose)
    {
        this.time = time;
        this.glucose = glucose;
    }

    public Instant time() {
        return time;
    }

    public BloodGlucose glucose() {
        return glucose;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BloodPoint) obj;
        return Objects.equals(this.time, that.time) &&
                Objects.equals(this.glucose, that.glucose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, glucose);
    }

    @Override
    public String toString() {
        return "BloodPoint[" +
                "time=" + time + ", " +
                "glucose=" + glucose + ']';
    }

}
