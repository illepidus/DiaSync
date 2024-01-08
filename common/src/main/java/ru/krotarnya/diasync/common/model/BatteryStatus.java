package ru.krotarnya.diasync.common.model;

import ru.krotarnya.diasync.common.DefaultObject;

public class BatteryStatus extends DefaultObject {
    private final boolean isCharging;
    private final double chargeLevel;

    public BatteryStatus(boolean isCharging, double chargeLevel) {
        this.isCharging = isCharging;
        this.chargeLevel = chargeLevel;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public double chargeLevel() {
        return chargeLevel;
    }

    public int chargePercentRounded() {
        return Math.toIntExact(Math.round(100 * chargeLevel()));
    }
}
