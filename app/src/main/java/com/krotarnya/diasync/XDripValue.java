package com.krotarnya.diasync;

import android.os.Bundle;

public class XDripValue  extends Glucose {
    public XDripCalibration calibration;
    public long timestamp;
    public double value;
    public String arrow;

    XDripValue() {
        this(new Bundle());
    }

    XDripValue(Bundle bundle) {
        timestamp = bundle.getLong("xdrip_timestamp", 0);
        value = bundle.getDouble("xdrip_value", 0);
        arrow = bundle.getString("xdrip_arrow", "none");
        calibration = new XDripCalibration(bundle);
    }
}
