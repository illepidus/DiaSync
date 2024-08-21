package ru.krotarnya.diasync.common.util;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class DateTimeUtil {
    public static ZonedDateTime toStartOfNMinutes(ZonedDateTime dateTime, int n) {
        int minute = Math.abs(n) % 60;

        return dateTime
                .withMinute(minute == 0 ? 0 : dateTime.getMinute() / minute * minute)
                .withSecond(0)
                .withNano(0);
    }

    public static ZonedDateTime toStartOfHour(ZonedDateTime dateTime) {
        return toStartOfNMinutes(dateTime, 0);
    }

    public static Instant toStartOfMinute(Instant timestamp) {
        return timestamp.atZone(TimeZone.getDefault().toZoneId())
                .withSecond(0)
                .withNano(0)
                .toInstant();
    }

    public static Instant toStartOfSecond(Instant timestamp) {
        return timestamp.atZone(TimeZone.getDefault().toZoneId())
                .withNano(0)
                .toInstant();
    }
}
