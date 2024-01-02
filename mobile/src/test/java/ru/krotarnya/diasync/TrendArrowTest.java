package ru.krotarnya.diasync;

import junit.framework.TestCase;

import ru.krotarnya.diasync.model.TrendArrow;

public class TrendArrowTest extends TestCase {
    public void testOf() {
        assertEquals(TrendArrow.DOUBLE_DOWN, TrendArrow.of(-500.0));
        assertEquals(TrendArrow.SINGLE_DOWN, TrendArrow.of(-10));
        assertEquals(TrendArrow.DOWN_45,     TrendArrow.of(-5.0));
        assertEquals(TrendArrow.FLAT,        TrendArrow.of(0.0));
        assertEquals(TrendArrow.UP_45,       TrendArrow.of(5.0));
        assertEquals(TrendArrow.SINGLE_UP,   TrendArrow.of(10.0));
        assertEquals(TrendArrow.DOUBLE_UP,   TrendArrow.of(500.0));
    }
}