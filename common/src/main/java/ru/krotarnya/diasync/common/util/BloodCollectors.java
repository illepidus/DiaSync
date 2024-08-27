package ru.krotarnya.diasync.common.util;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collector;

import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodGlucoseStats;

public class BloodCollectors {
    private static final Collector<BloodGlucose, Queue<BloodGlucose>, Optional<BloodGlucoseStats>> bloodGlucoseCollector = Collector.of(
            ConcurrentLinkedQueue::new,
            Queue::offer,
            Functions::union,
            BloodCollectors::finish,
            Collector.Characteristics.UNORDERED,
            Collector.Characteristics.CONCURRENT);

    public static Collector<BloodGlucose, ?, Optional<BloodGlucoseStats>> bloodGlucoseCollector() {
        return bloodGlucoseCollector;
    }

    private static Optional<BloodGlucoseStats> finish(Collection<BloodGlucose> bloodGlucose) {
        if (bloodGlucose.isEmpty()) return Optional.empty();

        DoubleSummaryStatistics bloodStatistics = bloodGlucose.stream()
                .mapToDouble(BloodGlucose::mgdl)
                .summaryStatistics();

        return Optional.of(new BloodGlucoseStats() {
            @Override
            public BloodGlucose minBloodGlucose() {
                return BloodGlucose.consMgdl(bloodStatistics.getMin());
            }

            @Override
            public BloodGlucose maxBloodGlucose() {
                return BloodGlucose.consMgdl(bloodStatistics.getMax());
            }

            @Override
            public BloodGlucose avgBloodGlucose() {
                return BloodGlucose.consMgdl(bloodStatistics.getAverage());
            }
        });
    }
}
