package com.krotarnya.diasync;

import java.text.DecimalFormat;
import java.text.ParseException;

public abstract class Glucose {
    static double mgdlToMmol(double v) {
        return v * 0.0555;
    }

    static double mgdlToMmol(String v) {
        return mgdlToMmol(glucose(v));
    }

    static double mmolToMgdl(double v) {
        return v * 18;
    }
    static double mmolToMgdl(String v) {
        return mmolToMgdl(glucose(v));
    }

    static double glucose(String v) {
        DecimalFormat format = new DecimalFormat();
        if (v == null) return 0;
        try {
            Number n = format.parse(v);
            return (n == null) ? 0 : n.doubleValue();
        } catch (ParseException e) {
            return 0;
        }
    }

    static String stringMmol(double v) {
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(v);
    }

    static String stringMgdl(double v) {
        DecimalFormat format = new DecimalFormat("0");
        return format.format(v);
    }

}
