package ru.krotarnya.diasync.common.model;

import ru.krotarnya.diasync.common.DefaultObject;

public class WatchSensorReadings extends DefaultObject {
    private final int steps;

    public WatchSensorReadings(int steps) {
        this.steps = steps;
    }

    public int steps() {
        return steps;
    }
}
