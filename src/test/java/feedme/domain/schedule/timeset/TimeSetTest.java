package feedme.domain.schedule.timeset;

import static org.junit.jupiter.api.Assertions.*;

import feedme.util.InfInstant;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TimeSetTest {
    @Test
    public void testInstantTimeSpan() {
        InfInstant time1 = InfInstant.of(Instant.ofEpochMilli(1700471244096L));
        InfInstant time2 = InfInstant.of(Instant.ofEpochMilli(1701471266063L));
        List<InfInstant> testTimes = List.of(
            InfInstant.of(Instant.MIN.plusNanos(1)),
            time1,
            InfInstant.now(),
            InfInstant.of(Instant.MAX.minusNanos(2))
        );
        for (InfInstant time : testTimes) {
            TimeSpan timeSpan = instantTimeSpan(time);
            assertTrue(timeSpan.getAt(time).isPresent());
            assertTrue(timeSpan.contains(time));
            assertFalse(timeSpan.contains(time2));
        }
        TimeSpan timeSpan = instantTimeSpan(time2);
        assertEquals(instantTimeSpan(time2), timeSpan.getNext(time1).orElseThrow());
        assertEquals(time2, timeSpan.getNext(time1).orElseThrow().start());
        assertEquals(time2.plusNanos(1), timeSpan.getNext(time1).orElseThrow().end());
        assertFalse(timeSpan.getPrevious(time1).isPresent());
        assertFalse(timeSpan.getAt(time1).isPresent());
    }

    @Test
    public void testSimpleTimeSpan() {
        Instant time1 = Instant.ofEpochMilli(1600471244096L);
        Instant time2 = Instant.ofEpochMilli(1701171256063L);
        TimeSpan timeSpan = TimeSpan.withBounds(time1, time2);
        assertFalse(timeSpan.contains(time1.minusMillis(1)));
        assertTrue(timeSpan.contains(time1));
        assertTrue(timeSpan.contains(time1.plusMillis(1)));
        assertTrue(timeSpan.contains(time2.minusMillis(1)));
        assertFalse(timeSpan.isContiguousWith(instantTimeSpan(time1.minusMillis(1))));
        assertTrue(timeSpan.isContiguousWith(instantTimeSpan(time1)));
        assertTrue(timeSpan.isContiguousWith(instantTimeSpan(time1.plusMillis(1))));
        assertTrue(timeSpan.isContiguousWith(instantTimeSpan(time2.minusMillis(1))));
        assertFalse(timeSpan.contains(time2));
        assertFalse(timeSpan.contains(time2.plusMillis(1)));
        // notice touching time spans are contiguous
        assertTrue(timeSpan.isContiguousWith(instantTimeSpan(time2)));
        assertFalse(timeSpan.isContiguousWith(instantTimeSpan(time2.plusMillis(1))));

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
        TimeSpan tsA = TimeSpan.withBounds(time1, time2);
        TimeSpan tsB = TimeSpan.withBounds(time2, time3);
        TimeSpan tsC = TimeSpan.withBounds(time3, time4);
        TimeSpan tsD = TimeSpan.withBounds(time1, time3);
        TimeSpan tsE = TimeSpan.withBounds(time2, time4);
        TimeSpan tsF = TimeSpan.withBounds(time1, time4);

        assertEquals(tsA, tsA.unionWith(tsA));
        for (TimeSpan ts : List.of(tsB, tsC, tsD, tsE, tsF)) {
            assertNotEquals(instantTimeSpan(time2), ts);
            assertNotEquals(TimeSet.empty(), ts);
            assertNotEquals(ts, tsA);
            assertNotEquals(ts, tsA.unionWith(tsA));
        }
        assertEquals(tsD, tsA.unionWith(tsB));
        assertEquals(tsD, tsB.unionWith(tsA));
        assertEquals(tsE, tsB.unionWith(tsC));
        assertEquals(tsF, tsA.unionWith(tsB).unionWith(tsC));
        assertEquals(tsF, tsA.unionWith(tsB).unionWith(tsC).unionWith(tsD).unionWith(tsA));
        assertEquals(tsF, tsD.unionWith(tsE));
        assertEquals(TimeSet.empty(), tsA.intersectWith(tsB));
        assertEquals(TimeSet.empty(), tsA.intersectWith(tsC));
        assertEquals(TimeSet.empty(), tsA.intersectWith(tsE));
        assertEquals(tsB, tsF.intersectWith(tsB));
        assertEquals(tsB, tsB.intersectWith(tsD).intersectWith(tsF));
        assertEquals(tsB, tsD.intersectWith(tsE));
        assertEquals(tsB, tsE.intersectWith(tsD));
    }

    static Instant[] t = new Instant[9];

    static TimeSegment spanA;
    static TimeSegment spanB;
    static TimeSegment spanC;
    static TimeSet setA;
    static TimeSet emptySet = TimeSet.empty();
    @BeforeAll
    public static void setupTimes() {
        Instant time = Instant.ofEpochMilli(1328205660000L);
        for (int i = 0; i < 9; i++) {
            t[i] = time;
            time = time.plusSeconds(60);
        }
        spanA = TimeSegment.withBounds(t[1], t[3]);
        spanB = TimeSegment.withBounds(t[3], t[5]);
        spanC = TimeSegment.withBounds(t[5], t[7]);
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
        // TODO fix this test, somehow return measurable time sets
        // assertEquals(Duration.ofMinutes(4), setA.getDuration());

        // test equals
        assertEquals(setA, spanA.unionWith(spanC));
        assertEquals(setA, spanC.unionWith(spanA));
    }

    @Test
    public void testSimpleTimeSetOperations() {
        // TODO: test associativity
        // empty set unions
        assertEquals(emptySet, emptySet.unionWith(emptySet));
        assertEquals(spanB, emptySet.unionWith(spanB));
        assertEquals(setA, emptySet.unionWith(setA));
        assertEquals(setA, setA.unionWith(emptySet));
        assertEquals(spanC, emptySet.unionWith(spanC));

        // empty set intersections
        assertEquals(emptySet, emptySet.intersectWith(spanA));
        assertEquals(emptySet, emptySet.intersectWith(spanC));
        assertEquals(emptySet, emptySet.intersectWith(setA));
        assertEquals(emptySet, setA.intersectWith(emptySet));
        assertEquals(emptySet, setA.intersectWith(spanB));
        assertEquals(emptySet, spanA.intersectWith(emptySet));

        // set A unions
        assertEquals(setA, setA.unionWith(spanA.unionWith(spanC)));
        assertEquals(setA, spanA.unionWith(spanC).unionWith(setA));
        assertEquals(setA, setA.unionWith(setA));
        assertEquals(setA, spanA.unionWith(setA));
        assertEquals(setA, setA.unionWith(spanA.unionWith(spanC)).unionWith(spanC));
        assertNotEquals(setA, setA.unionWith(instantTimeSpan(t[1].minusNanos(1))));
        assertEquals(setA, setA.unionWith(instantTimeSpan(t[1])));
        assertEquals(setA, setA.unionWith(instantTimeSpan(t[3].minusNanos(1))));
        assertNotEquals(setA, setA.unionWith(instantTimeSpan(t[3])));
        assertNotEquals(setA, setA.unionWith(instantTimeSpan(t[4])));
        // TODO: equality operator
        assertEquals(TimeSpan.withBounds(t[1], t[7]), setA.unionWith(spanB));

        // set A intersections
        assertEquals(setA, setA.intersectWith(setA));
        assertEquals(spanA, setA.intersectWith(TimeSpan.withBounds(t[1], t[4])));
        assertEquals(spanC, setA.intersectWith(TimeSpan.withBounds(t[3], t[8])));
        assertEquals(spanA.intersectWith(TimeSpan.withBounds(t[2], t[4])).unionWith(spanC.intersectWith(TimeSpan.withBounds(t[4], t[6]))), setA.intersectWith(
	        TimeSpan.withBounds(t[2], t[6])));

        // other operations
        assertEquals(TimeSpan.withBounds(t[1], t[7]), spanA.unionWith(spanB).unionWith(spanC));
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

    private static TimeSpan instantTimeSpan(Instant time) {
        return TimeSpan.withBounds(time, time.plusNanos(1));
    }

    private static TimeSpan instantTimeSpan(InfInstant time) {
        return TimeSpan.withBounds(time, time.plusNanos(1));
    }
}