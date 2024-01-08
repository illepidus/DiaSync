package ru.krotarnya.diasync.common;

import static org.junit.Assert.*;

import junit.framework.TestCase;

public class DefaultObjectTest extends TestCase {
    public void testEqualsSimpleObject() {
        assertEquals(SimpleObject.consEmpty(), SimpleObject.consEmpty());
        assertEquals(SimpleObject.consVariant1(), SimpleObject.consVariant1());
        assertEquals(SimpleObject.consVariant2(), SimpleObject.consVariant2());
        assertEquals(SimpleObject.consNullFieldVariant(), SimpleObject.consNullFieldVariant());
    }

    public void testNonEquals() {
        assertNotEquals(SimpleObject.consEmpty(), SimpleObject.consVariant1());
        assertNotEquals(SimpleObject.consVariant1(), SimpleObject.consVariant2());
        assertNotEquals(SimpleObject.consVariant2(), SimpleObject.consNullFieldVariant());
        assertNotEquals(SimpleObject.consNullFieldVariant(), SimpleObject.consEmpty());
    }

    public void testHashCodeMatches() {
        assertEquals(SimpleObject.consEmpty().hashCode(), SimpleObject.consEmpty().hashCode());
        assertEquals(SimpleObject.consVariant1().hashCode(), SimpleObject.consVariant1().hashCode());
        assertEquals(SimpleObject.consVariant2().hashCode(), SimpleObject.consVariant2().hashCode());
        assertEquals(SimpleObject.consNullFieldVariant().hashCode(), SimpleObject.consNullFieldVariant().hashCode());
    }

    public void testHashCodeDiffer() {
        assertNotEquals(SimpleObject.consEmpty().hashCode(), SimpleObject.consVariant1().hashCode());
        assertNotEquals(SimpleObject.consVariant1().hashCode(), SimpleObject.consVariant2().hashCode());
        assertNotEquals(SimpleObject.consVariant2().hashCode(), SimpleObject.consNullFieldVariant().hashCode());
        assertNotEquals(SimpleObject.consNullFieldVariant().hashCode(), SimpleObject.consEmpty().hashCode());
    }

    public void testToString() {
        assertEquals("[b=false,d=0.0,i=0,s=]", SimpleObject.consEmpty().toString());
        assertEquals("[b=true,d=1.0,i=1,s=1]", SimpleObject.consVariant1().toString());
        assertEquals("[b=true,d=2.0,i=2,s=2]", SimpleObject.consVariant2().toString());
        assertEquals("[b=true,d=3.0,i=3,s=<null>]", SimpleObject.consNullFieldVariant().toString());
    }

    private static class SimpleObject extends DefaultObject {
        public final boolean b;
        public final double d;
        public final int i;
        public final String s;

        private SimpleObject(boolean b, double d, int i, String s) {
            this.b = b;
            this.d = d;
            this.i = i;
            this.s = s;
        }

        private static SimpleObject consEmpty() {
            return new SimpleObject(false, 0, 0, "");
        }

        private static SimpleObject consVariant1() {
            return new SimpleObject(true, 1, 1, "1");
        }

        private static SimpleObject consVariant2() {
            return new SimpleObject(true, 2, 2, "2");
        }

        private static SimpleObject consNullFieldVariant() {
            return new SimpleObject(true, 3, 3, null);
        }
    }
}