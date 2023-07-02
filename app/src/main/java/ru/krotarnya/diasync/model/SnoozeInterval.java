package ru.krotarnya.diasync.model;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum SnoozeInterval {
    _5M(Duration.ofMinutes(5), "5 minutes"),
    _10M(Duration.ofMinutes(10), "10 minutes"),
    _15M(Duration.ofMinutes(15), "15 minutes"),
    _20M(Duration.ofMinutes(20), "20 minutes"),
    _30M(Duration.ofMinutes(30), "30 minutes"),
    _1H(Duration.ofHours(1), "1 hour"),
    _2H(Duration.ofHours(2), "2 hours"),
    _4H(Duration.ofHours(4), "4 hours"),
    _6H(Duration.ofHours(6), "6 hours"),
    _8H(Duration.ofHours(8), "8 hours"),
    _10H(Duration.ofHours(10), "10 hours"),
    _12H(Duration.ofHours(12), "12 hours"),
    _24H(Duration.ofHours(24), "24 hours"),
    ;

    private static final List<SnoozeInterval> SORTED_VALUES = Arrays.stream(values())
            .sorted(Comparator.comparingLong(i -> i.duration.toMillis()))
            .collect(Collectors.toList());

    private final Duration duration;
    private final String displayedText;


    SnoozeInterval(Duration duration, String displayedText) {
        this.duration = duration;
        this.displayedText = displayedText;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getDisplayedText() {
        return displayedText;
    }

    public static int getTotalCount() {
        return SORTED_VALUES.size();
    }

    public static SnoozeInterval getByOrderOrDefault(int order) {
        return (order >= 0 && order < getTotalCount())
                ? SORTED_VALUES.get(order)
                : SORTED_VALUES.get(0);
    }
}
