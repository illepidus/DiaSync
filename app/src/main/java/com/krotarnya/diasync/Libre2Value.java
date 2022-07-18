package com.krotarnya.diasync;

import android.os.Bundle;

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

    public double getCalibratedValue() {
        return value * xdrip_calibration.slope + xdrip_calibration.intercept;
    }

    public double getCalibratedMmolValue() {
        return Glucose.mgdlToMmol(getCalibratedValue());
    }
}