package com.krotarnya.diasync;

import android.os.Bundle;

public class XDripCalibration {
    public long timestamp;
    public double slope;
    public double intercept;

    XDripCalibration(Bundle bundle) {
        timestamp = bundle.getLong("calibration_timestamp", 0);
        slope = bundle.getDouble("calibration_slope", 1);
        intercept = bundle.getDouble("calibration_intercept", 0);
    }

    XDripCalibration() {
        this(new Bundle());
    }
}

