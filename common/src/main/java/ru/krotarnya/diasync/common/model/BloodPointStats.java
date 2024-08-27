package ru.krotarnya.diasync.common.model;

import java.time.Instant;

public interface BloodPointStats extends BloodGlucoseStats {
    Instant minTimestamp();

    Instant maxTimestamp();
}
