package ru.krotarnya.diasync.common.model;

import junit.framework.TestCase;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BloodChartTest extends TestCase {
    private static final Random RANDOM = new Random(0);
    private static final BloodChart.Params DEFAULT_PARAMS = new BloodChart.Params(
            BloodGlucoseUnit.MMOL,
            BloodGlucose.consMmol(3.9),
            BloodGlucose.consMmol(10.0),
            Instant.EPOCH,
            Instant.now(),
            new BloodChart.Colors(0, 0,0));

    public void testSerializeDeserialize() {
        List<BloodPoint> points = Stream.generate(this::getRandomBloodPoint)
                .limit(60)
                .collect(Collectors.toList());

        BloodChart source = new BloodChart(points, TrendArrow.NONE, DEFAULT_PARAMS);
        BloodChart result = BloodChart.deserialize(source.serialize());

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