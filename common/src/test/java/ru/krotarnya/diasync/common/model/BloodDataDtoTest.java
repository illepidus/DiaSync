package ru.krotarnya.diasync.common.model;

import junit.framework.TestCase;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BloodDataDtoTest extends TestCase {
    private static final Random RANDOM = new Random(0);
    private static final BloodData.Params DEFAULT_PARAMS = new BloodData.Params(
            BloodGlucoseUnit.MMOL,
            BloodGlucose.consMmol(3.9),
            BloodGlucose.consMmol(10.0),
            Duration.ofMinutes(60),
            new BloodData.Colors(0, 0, 0, 0, 0, 0));

    public void testSerializeDeserialize() throws Exception {
        List<BloodPoint> points = Stream.generate(this::getRandomBloodPoint)
                .limit(DEFAULT_PARAMS.timeWindow().getSeconds() / 60)
                .collect(Collectors.toList());

        BloodData source = new BloodData(points, TrendArrow.UNKNOWN, DEFAULT_PARAMS);
        BloodData result = BloodData.deserialize(source.serialize());

        assertEquals(source, result);
    }

    private BloodPoint getRandomBloodPoint() {
        return new BloodPoint(
                Instant.ofEpochSecond(RANDOM.nextLong(
                        Instant.MIN.getEpochSecond(),
                        Instant.MAX.getEpochSecond())),
                BloodGlucose.consMmol(RANDOM.nextDouble(2.2, 20.0)));
    }
}