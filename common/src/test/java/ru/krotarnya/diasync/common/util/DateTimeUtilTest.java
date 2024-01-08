package ru.krotarnya.diasync.common.util;

import junit.framework.TestCase;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtilTest extends TestCase {
    private static final ZoneId TIMEZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(TIMEZONE);

    private ZonedDateTime parseDateTime(String dateTime) {
        return ZonedDateTime.from(DATE_TIME_FORMATTER.parse(dateTime));
    }

    public void testToStartOf10Minutes() {
        var source = parseDateTime("2024-01-08 01:23:32");
        var expected = parseDateTime("2024-01-08 01:20:00");
        assertEquals(expected, DateTimeUtil.toStartOfNMinutes(source, 10));
    }

    public void testToStartOf15Minutes() {
        var source = parseDateTime("2024-01-08 01:23:32");
        var expected = parseDateTime("2024-01-08 01:15:00");
        assertEquals(expected, DateTimeUtil.toStartOfNMinutes(source, 15));
    }

    public void testToStartOfHour() {
        var source = parseDateTime("2024-01-08 00:59:59");
        var expected = parseDateTime("2024-01-08 00:00:00");
        assertEquals(expected, DateTimeUtil.toStartOfHour(source));
    }
}