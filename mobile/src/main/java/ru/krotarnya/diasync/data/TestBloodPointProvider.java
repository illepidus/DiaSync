package ru.krotarnya.diasync.data;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodPoint;
import ru.krotarnya.diasync.common.util.DateTimeUtil;
import ru.krotarnya.diasync.settings.Settings;

public class TestBloodPointProvider implements BloodPointProvider {
    private static final Random random = new Random();
    private static final Duration INTERVAL = Duration.ofMinutes(1);
    private final Settings settings;

    public TestBloodPointProvider(Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<BloodPoint> get(Instant from, Instant to) {
        return Stream.iterate(DateTimeUtil.toStartOfSecond(from), i -> i.plus(INTERVAL))
                .limit(Duration.between(from, to).toMillis() / INTERVAL.toMillis())
                .map(this::consPoint)
                .collect(Collectors.toList());
    }

    private BloodPoint consPoint(Instant timestamp) {
        return new BloodPoint(timestamp, consGlucose());
    }

    private BloodGlucose consGlucose() {
        return BloodGlucose.consMgdl(random
                .doubles(settings.glucoseLow().mgdl(), settings.glucoseHigh().mgdl())
                .findFirst()
                .orElseThrow());
    }
}
