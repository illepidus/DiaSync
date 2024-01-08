package ru.krotarnya.diasync.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

import ru.krotarnya.diasync.common.DefaultObject;

public final class BloodPoint extends DefaultObject {
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
}
