package com.krotarnya.diasync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

public class Libre2Value {
    public XDripValue xdrip_value;
    public XDripCalibration xdrip_calibration;
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

    public double getValue() {
        return value;
    }

    public double getMmolValue() {
        return value * 0.0555;
    }

    public double getCalibratedValue() {
        return value * xdrip_calibration.slope + xdrip_calibration.intercept;
    }

    public double getCalibratedMmolValue() {
        return getCalibratedValue() * 0.0555;
    }
}