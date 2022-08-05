package ru.krotarnya.diasync;

import android.os.Bundle;

import androidx.annotation.NonNull;

public class XDripValue {
    public final XDripCalibration calibration;
    public long timestamp;
    public double value;
    public String arrow;

    XDripValue(Bundle bundle) {
        timestamp = bundle.getLong("xdrip_timestamp", 0);
        value = bundle.getDouble("xdrip_value", 0);
        arrow = bundle.getString("xdrip_arrow", "none");
        calibration = new XDripCalibration(bundle);
    }

    @NonNull
    @Override
    public String toString() {
        return  "xdrip_timestamp: " + timestamp + "\n" +
                "xdrip_value: " + value + "\n" +
                "xdrip_arrow:" + arrow + "\n" +
                calibration.toString();
    }
}
