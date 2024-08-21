package ru.krotarnya.diasync.data;

import java.time.Instant;
import java.util.List;

import ru.krotarnya.diasync.common.model.BloodPoint;

public interface BloodPointProvider {
    List<BloodPoint> get(Instant from, Instant to);
}
