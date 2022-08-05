package ru.krotarnya.diasync;

import android.os.Bundle;

import androidx.annotation.NonNull;

public class Libre2Value {
    public final XDripValue xdrip_value;
    public final XDripCalibration xdrip_calibration;
    public long timestamp;
    public String serial;
    public double value;

    Libre2Value(Bundle bundle) {
        timestamp = bundle.getLong("libre2_timestamp", 0);
        serial = bundle.getString("libre2_serial", "");
        value = bundle.getDouble("libre2_value", 0);
        xdrip_value = new XDripValue(bundle);
        xdrip_calibration = new XDripCalibration(bundle);
    }

    Libre2Value() {
        this(new Bundle());
    }

    public double getValue(boolean use_calibration) {
        return use_calibration ? value * xdrip_calibration.slope + xdrip_calibration.intercept : value;
    }

    public double getMmolValue(boolean use_calibration) {
        return Glucose.mgdlToMmol(getValue(use_calibration));
    }

    @NonNull
    @Override
    public String toString() {
        return  "libre2_timestamp: " + timestamp + "\n" +
                "libre2_serial: " + serial + "\n" +
                "libre2_value:" + value + "\n" +
                xdrip_value.toString();
    }
}