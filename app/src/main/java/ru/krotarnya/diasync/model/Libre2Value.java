package ru.krotarnya.diasync.model;

import android.os.Bundle;

import androidx.annotation.NonNull;

import ru.krotarnya.diasync.Glucose;

public class Libre2Value {
    public final XDripValue xDripvalue;
    public final XDripCalibration xDripCalibration;
    public long timestamp;
    public String serial;
    public double value;

    public Libre2Value(Bundle bundle) {
        timestamp = bundle.getLong("libre2_timestamp", 0);
        serial = bundle.getString("libre2_serial", "");
        value = bundle.getDouble("libre2_value", 0);
        xDripvalue = new XDripValue(bundle);
        xDripCalibration = new XDripCalibration(bundle);
    }

    public Libre2Value() {
        this(new Bundle());
    }

    public double getValue(boolean use_calibration) {
        return use_calibration ? value * xDripCalibration.slope + xDripCalibration.intercept : value;
    }

    public double getMmolValue(boolean use_calibration) {
        return Glucose.mgdlToMmol(getValue(use_calibration));
    }

    public boolean isLow(boolean use_calibrations) {
        return (getValue(use_calibrations) < Glucose.low());
    }

    public boolean isHigh(boolean use_calibrations) {
        return (getValue(use_calibrations) > Glucose.high());
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