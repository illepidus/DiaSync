package ru.krotarnya.diasync.common.model;

import junit.framework.TestCase;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WatchFaceBloodDataDtoTest extends TestCase {
    private static final Random RANDOM = new Random(0);
    private static final WatchFaceBloodData.Params DEFAULT_PARAMS = new WatchFaceBloodData.Params(
            BloodGlucoseUnit.MMOL,
            BloodGlucose.consMmol(3.9),
            BloodGlucose.consMmol(10.0),
            Duration.ofMinutes(60),
            new WatchFaceBloodData.Colors(0, 0,0, 0, 0, 0));

    public void testSerializeDeserialize() throws Exception {
        List<BloodPoint> points = Stream.generate(this::getRandomBloodPoint)
                .limit(DEFAULT_PARAMS.timeWindow().getSeconds() / 60)
                .collect(Collectors.toList());

        WatchFaceBloodData source = new WatchFaceBloodData(points, TrendArrow.NONE, DEFAULT_PARAMS);
        WatchFaceBloodData result = WatchFaceBloodData.deserialize(source.serialize());

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