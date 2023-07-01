package ru.krotarnya.diasync.model;

import android.os.Bundle;

import androidx.annotation.NonNull;

import ru.krotarnya.diasync.Glucose;

public class Libre2Value {
    public final XDripValue xDripvalue;
    public final XDripCalibration xDripCalibration;
    public final long timestamp;
    public final String serial;
    public final double value;

    public Libre2Value(XDripValue xDripvalue, XDripCalibration xDripCalibration, long timestamp, String serial, double value) {
        this.xDripvalue = xDripvalue;
        this.xDripCalibration = xDripCalibration;
        this.timestamp = timestamp;
        this.serial = serial;
        this.value = value;
    }

    public Libre2Value(Bundle bundle) {
        timestamp = bundle.getLong("libre2_timestamp", 0);
        serial = bundle.getString("libre2_serial", "");
        value = bundle.getDouble("libre2_value", 0);
        xDripvalue = new XDripValue(bundle);
        xDripCalibration = new XDripCalibration(bundle);
    }

    public double getValue(boolean useCalibration) {
        return useCalibration ? value * xDripCalibration.slope + xDripCalibration.intercept : value;
    }

    public double getMmolValue(boolean useCalibration) {
        return Glucose.mgdlToMmol(getValue(useCalibration));
    }

    public boolean isLow(boolean useCalibrations) {
        return (getValue(useCalibrations) < Glucose.low());
    }

    public boolean isHigh(boolean useCalibrations) {
        return (getValue(useCalibrations) > Glucose.high());
    }

    @NonNull
    @Override
    public String toString() {
        return  "libre2_timestamp: " + timestamp + "\n" +
                "libre2_serial: " + serial + "\n" +
                "libre2_value:" + value + "\n" +
                xDripvalue.toString();
    }
}