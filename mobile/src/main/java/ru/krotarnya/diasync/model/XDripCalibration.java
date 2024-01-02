package ru.krotarnya.diasync.model;

import android.os.Bundle;

import androidx.annotation.NonNull;

public class XDripCalibration {
    public final long timestamp;
    public final double slope;
    public final double intercept;

    public XDripCalibration(long timestamp, double slope, double intercept) {
        this.timestamp = timestamp;
        this.slope = slope;
        this.intercept = intercept;
    }

    public XDripCalibration(Bundle bundle) {
        timestamp = bundle.getLong("xdrip_calibration_timestamp", 0);
        slope = bundle.getDouble("xdrip_calibration_slope", 1);
        intercept = bundle.getDouble("xdrip_calibration_intercept", 0);
    }

    @NonNull
    @Override
    public String toString() {
        return  "calibration_timestamp: " + timestamp + "\n" +
                "calibration_slope: " + slope + "\n" +
                "calibration_intercept:" + intercept;
    }
}

