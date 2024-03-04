package feedme.domain.schedule.timeset;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeSetTest {
    @Test
    public void testInstantTimeSpan() {
        Instant time1 = Instant.ofEpochMilli(1700471244096L);
        Instant time2 = Instant.ofEpochMilli(1701471266063L);
        for (Instant time : List.of(Instant.MIN, time1, Instant.now(), Instant.MAX.minusNanos(1))) {
            TimeSpan timeSpan = TimeSpan.ofInstant(time);
            assertTrue(timeSpan.getAt(time).isPresent());
            assertTrue(timeSpan.contains(time));
            assertFalse(timeSpan.contains(time2));
        }
        TimeSpan timeSpan = TimeSpan.ofInstant(time2);
        assertEquals(TimeSpan.ofInstant(time2), timeSpan.getNext(time1).orElseThrow());
        assertEquals(time2, timeSpan.getNext(time1).orElseThrow().startTime());
        assertEquals(time2.plusNanos(1), timeSpan.getNext(time1).orElseThrow().endTime());
        assertFalse(timeSpan.getPrevious(time1).isPresent());
        assertFalse(timeSpan.getAt(time1).isPresent());
    }

    @Test
    public void testSimpleTimeSpan() {
        Instant time1 = Instant.ofEpochMilli(1600471244096L);
        Instant time2 = Instant.ofEpochMilli(1701171256063L);
        TimeSpan timeSpan = TimeSpan.ofInstants(time1, time2);
        assertFalse(timeSpan.contains(time1.minusMillis(1)));
        assertTrue(timeSpan.contains(time1));
        assertTrue(timeSpan.contains(time1.plusMillis(1)));
        assertTrue(timeSpan.contains(time2.minusMillis(1)));
        assertFalse(timeSpan.isContiguousWith(TimeSpan.ofInstant(time1.minusMillis(1))));
        assertTrue(timeSpan.isContiguousWith(TimeSpan.ofInstant(time1)));
        assertTrue(timeSpan.isContiguousWith(TimeSpan.ofInstant(time1.plusMillis(1))));
        assertTrue(timeSpan.isContiguousWith(TimeSpan.ofInstant(time2.minusMillis(1))));
        assertFalse(timeSpan.contains(time2));
        assertFalse(timeSpan.contains(time2.plusMillis(1)));
        // notice touching time spans are contiguous
        assertTrue(timeSpan.isContiguousWith(TimeSpan.ofInstant(time2)));
        assertFalse(timeSpan.isContiguousWith(TimeSpan.ofInstant(time2.plusMillis(1))));

        Instant justBeforeTime1 = time1.minusNanos(1);
        Instant justAfterTime1 = time1.plusNanos(1);
        Instant justBeforeTime2 = time2.minusNanos(1);
        Instant justAfterTime2 = time2.plusNanos(1);
        assertFalse(timeSpan.getPrevious(justBeforeTime1).isPresent());
        assertFalse(timeSpan.getAt(justBeforeTime1).isPresent());
        assertEquals(timeSpan, timeSpan.getNext(justBeforeTime1).orElseThrow());
        assertFalse(timeSpan.getPrevious(time1).isPresent());
        assertEquals(timeSpan, timeSpan.getAt(time1).orElseThrow());
        assertFalse(timeSpan.getNext(time1).isPresent());
        assertFalse(timeSpan.getPrevious(justAfterTime1).isPresent());
        assertEquals(timeSpan, timeSpan.getAt(justAfterTime1).orElseThrow());
        assertFalse(timeSpan.getNext(justAfterTime1).isPresent());
        assertFalse(timeSpan.getPrevious(justBeforeTime2).isPresent());
        assertEquals(timeSpan, timeSpan.getAt(justBeforeTime2).orElseThrow());
        assertFalse(timeSpan.getNext(justBeforeTime2).isPresent());
        assertEquals(timeSpan, timeSpan.getPrevious(time2).orElseThrow());
        assertFalse(timeSpan.getAt(time2).isPresent());
        assertFalse(timeSpan.getNext(time2).isPresent());
        assertEquals(timeSpan, timeSpan.getPrevious(justAfterTime2).orElseThrow());
        assertFalse(timeSpan.getAt(justAfterTime2).isPresent());
        assertFalse(timeSpan.getNext(justAfterTime2).isPresent());
    }

    @Test
    public void testSimpleTwoTimeSpans() {
        Instant time1 = Instant.ofEpochMilli(1600471244096L);
        Instant time2 = time1.plus(Duration.ofMinutes(1));
        Instant time3 = time2.plus(Duration.ofMinutes(1));
        Instant time4 = time3.plus(Duration.ofMinutes(1));
        TimeSpan tsA = TimeSpan.ofInstants(time1, time2);
        TimeSpan tsB = TimeSpan.ofInstants(time2, time3);
        TimeSpan tsC = TimeSpan.ofInstants(time3, time4);
        TimeSpan tsD = TimeSpan.ofInstants(time1, time3);
        TimeSpan tsE = TimeSpan.ofInstants(time2, time4);
        TimeSpan tsF = TimeSpan.ofInstants(time1, time4);

        assertEquals(tsA, tsA.unionWith(tsA));
        for (TimeSpan ts : List.of(tsB, tsC, tsD, tsE, tsF)) {
            assertNotEquals(TimeSpan.ofInstant(time2), ts);
            assertNotEquals(TimeSpan.EMPTY, ts);
            assertNotEquals(ts, tsA);
            assertNotEquals(ts, tsA.unionWith(tsA));
        }
        assertEquals(tsD, tsA.unionWith(tsB));
        assertEquals(tsD, tsB.unionWith(tsA));
        assertEquals(tsE, tsB.unionWith(tsC));
        assertEquals(tsF, tsA.unionWith(tsB).unionWith(tsC));
        assertEquals(tsF, tsA.unionWith(tsB).unionWith(tsC).unionWith(tsD).unionWithTimeSpan(tsA));
        assertEquals(tsF, tsD.unionWith(tsE));
        assertEquals(TimeSet.EMPTY, tsA.intersectWith(tsB));
        assertEquals(TimeSet.EMPTY, tsA.intersectWith(tsC));
        assertEquals(TimeSet.EMPTY, tsA.intersectWith(tsE));
        assertEquals(tsB, tsF.intersectWith(tsB));
        assertEquals(tsB, tsB.intersectWith(tsD).intersectWith(tsF));
        assertEquals(tsB, tsD.intersectWith(tsE));
        assertEquals(tsB, tsE.intersectWith(tsD));
    }

    static Instant[] t = new Instant[9];

    static TimeSpan spanA;
    static TimeSpan spanB;
    static TimeSpan spanC;
    static MeasurableTimeSet setA;
    static TimeSet emptySet = TimeSet.EMPTY;
    @BeforeAll
    public static void setupTimes() {
        Instant time = Instant.ofEpochMilli(1328205660000L);
        for (int i = 0; i < 9; i++) {
            t[i] = time;
            time = time.plusSeconds(60);
        }
        spanA = TimeSpan.ofInstants(t[1], t[3]);
        spanB = TimeSpan.ofInstants(t[3], t[5]);
        spanC = TimeSpan.ofInstants(t[5], t[7]);
        setA = spanA.unionWith(spanC);
    }

    @Test
    public void testSimpleTimeSetMethods() {
        new AssertionSet(emptySet, t[0]).prevEmpty().atEmpty().nextEmpty();

        new AssertionSet(setA, t[0]).prevEmpty().atEmpty().nextEquals(spanA);
        new AssertionSet(setA, t[1]).prevEmpty().atEquals(spanA).nextEquals(spanC);
        new AssertionSet(setA, t[2]).prevEmpty().atEquals(spanA).nextEquals(spanC);
        new AssertionSet(setA, t[3]).prevEquals(spanA).atEmpty().nextEquals(spanC);
        new AssertionSet(setA, t[4]).prevEquals(spanA).atEmpty().nextEquals(spanC);
        new AssertionSet(setA, t[5]).prevEquals(spanA).atEquals(spanC).nextEmpty();
        new AssertionSet(setA, t[6]).prevEquals(spanA).atEquals(spanC).nextEmpty();
        new AssertionSet(setA, t[7]).prevEquals(spanC).atEmpty().nextEmpty();
        new AssertionSet(setA, t[8]).prevEquals(spanC).atEmpty().nextEmpty();

        assertEquals(Duration.ofMinutes(2), spanB.getDuration());
        assertEquals(Duration.ofMinutes(4), setA.getDuration());

        // test equals
        assertEquals(setA, spanA.unionWith(spanC));
        assertEquals(setA, spanC.unionWithTimeSpan(spanA));
    }

    @Test
    public void testSimpleTimeSetOperations() {
        // empty set unions
        assertEquals(emptySet, emptySet.unionWith(emptySet));
        assertEquals(spanB, emptySet.unionWith(spanB));
        assertEquals(setA, emptySet.unionWith(setA));
        assertEquals(setA, setA.unionWith(emptySet));
        assertEquals(spanC, emptySet.unionWithTimeSpan(spanC));

        // empty set intersections
        assertEquals(emptySet, emptySet.intersectWith(spanA));
        assertEquals(emptySet, emptySet.intersectWithTimeSpan(spanC));
        assertEquals(emptySet, emptySet.intersectWith(setA));
        assertEquals(emptySet, setA.intersectWith(emptySet));
        assertEquals(emptySet, setA.intersectWith(spanB));
        assertEquals(emptySet, spanA.intersectWith(emptySet));

        // set A unions
        assertEquals(setA, setA.unionWith(spanA.unionWithTimeSpan(spanC)));
        assertEquals(setA, spanA.unionWith(spanC).unionWith(setA));
        assertEquals(setA, setA.unionWith(setA));
        assertEquals(setA, spanA.unionWith(setA));
        assertEquals(setA, setA.unionWith(spanA.unionWithTimeSpan(spanC)).unionWith(spanC));
        assertNotEquals(setA, setA.unionWith(TimeSpan.ofInstant(t[1].minusNanos(1))));
        assertEquals(setA, setA.unionWith(TimeSpan.ofInstant(t[1])));
        assertEquals(setA, setA.unionWith(TimeSpan.ofInstant(t[3].minusNanos(1))));
        assertNotEquals(setA, setA.unionWith(TimeSpan.ofInstant(t[3])));
        assertNotEquals(setA, setA.unionWith(TimeSpan.ofInstant(t[4])));
        assertEquals(TimeSpan.ofInstants(t[1], t[7]), setA.unionWith(spanB));

        // set A intersections
        assertEquals(setA, setA.intersectWith(setA));
        assertEquals(spanA, setA.intersectWith(TimeSpan.ofInstants(t[1], t[4])));
        assertEquals(spanC, setA.intersectWith(TimeSpan.ofInstants(t[3], t[8])));
        assertEquals(spanA.intersectWith(TimeSpan.ofInstants(t[2], t[4])).unionWith(spanC.intersectWith(TimeSpan.ofInstants(t[4], t[6]))), setA.intersectWith(TimeSpan.ofInstants(t[2], t[6])));

        // other operations
        assertEquals(TimeSpan.ofInstants(t[1], t[7]), spanA.unionWith(spanB).unionWith(spanC));
    }

    public static class AssertionSet {
        private final TimeSet timeSet;
        private final Instant time;

        public AssertionSet(TimeSet timeSet, Instant time) {
            this.timeSet = timeSet;
            this.time = time;
        }

        public AssertionSet prevEmpty() {
            assertFalse(timeSet.getPrevious(time).isPresent());
            return this;
        }
        public AssertionSet prevEquals(TimeSet set) {
            assertEquals(set, timeSet.getPrevious(time).orElseThrow());
            return this;
        }

        public AssertionSet atEmpty() {
            assertFalse(timeSet.getAt(time).isPresent());
            return this;
        }
        public AssertionSet atEquals(TimeSet set) {
            assertEquals(set, timeSet.getAt(time).orElseThrow());
            return this;
        }

        public AssertionSet nextEmpty() {
            assertFalse(timeSet.getNext(time).isPresent());
            return this;
        }
        public AssertionSet nextEquals(TimeSet set) {
            assertEquals(set, timeSet.getNext(time).orElseThrow());
            return this;
        }
    }
}