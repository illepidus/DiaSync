package ru.krotarnya.diasync.model;

import android.os.Bundle;

import androidx.annotation.NonNull;

public class XDripValue {
    public final XDripCalibration calibration;
    public final long timestamp;
    public final double value;
    public final String arrow;

    public XDripValue(XDripCalibration calibration, long timestamp, double value, String arrow) {
        this.calibration = calibration;
        this.timestamp = timestamp;
        this.value = value;
        this.arrow = arrow;
    }

    XDripValue(Bundle bundle) {
        calibration = new XDripCalibration(bundle);
        timestamp = bundle.getLong("xdrip_timestamp", 0);
        value = bundle.getDouble("xdrip_value", 0);
        arrow = bundle.getString("xdrip_arrow", "none");
    }

    @NonNull
    @Override
    public String toString() {
        return "xdrip_timestamp: " + timestamp + "\n" +
                "xdrip_value: " + value + "\n" +
                "xdrip_arrow:" + arrow + "\n" +
                calibration.toString();
    }
}
