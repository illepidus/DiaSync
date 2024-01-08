package ru.krotarnya.diasync.common.util;

public class BloodUtil {
    private static final double MMOL_TO_MGDL = 18.0182d;
    public static final double MGDL_TO_MMOL = 1 / MMOL_TO_MGDL;

    public static double mgdlToMmol(double mgdl) {
        return mgdl * MGDL_TO_MMOL;
    }

    public static double mmolToMgdl(double mmol) {
        return mmol * MMOL_TO_MGDL;
    }
}
