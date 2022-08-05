package ru.krotarnya.diasync;

import android.os.Bundle;

public class XDripCalibration {
    public long timestamp;
    public double slope;
    public double intercept;

    XDripCalibration(Bundle bundle) {
        timestamp = bundle.getLong("xdrip_calibration_timestamp", 0);
        slope = bundle.getDouble("xdrip_calibration_slope", 1);
        intercept = bundle.getDouble("xdrip_calibration_intercept", 0);
    }

    @Override
    public String toString() {
        return  "calibration_timestamp: " + timestamp + "\n" +
                "calibration_slope: " + slope + "\n" +
                "calibration_intercept:" + intercept;
    }
}

