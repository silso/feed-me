package feedme.domain.schedule.timeset


import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class TimeSetUtilsTest extends Specification {
    def "FindContiguousEndTime for time spans"() {
        given:
        def t = new Instant[9]
        def time = Instant.ofEpochMilli(1328205600000L)
        for (int i = 0; i < 9; i++) {
            t[i] = time
            time = time.plusSeconds(60)
        }

        expect:
        TimeSetUtils.findContiguousEndTime(TimeSpan.ofInstants(t[a1], t[a2]), TimeSpan.ofInstants(t[b1], t[b2]), t[from]).map(res -> res == t[c] && !empty).orElse(empty)

        where:
        a1 | a2 | b1 | b2 | from || c | empty
        1  | 2  | 3  | 5  | 0    || 0 | true
        0  | 2  | 3  | 5  | 0    || 2 | false
        0  | 2  | 3  | 5  | 1    || 2 | false
        0  | 2  | 3  | 5  | 2    || 0 | true
        0  | 2  | 3  | 5  | 3    || 5 | false
        0  | 2  | 3  | 5  | 5    || 0 | true
        1  | 2  | 3  | 5  | 7    || 0 | true
        1  | 2  | 2  | 4  | 0    || 0 | true
        1  | 2  | 2  | 4  | 1    || 4 | false
        0  | 2  | 1  | 4  | 0    || 4 | false
        0  | 2  | 1  | 4  | 1    || 4 | false
        0  | 2  | 0  | 4  | 2    || 4 | false
        0  | 2  | 0  | 4  | 3    || 4 | false
        0  | 3  | 0  | 2  | 0    || 3 | false
        0  | 3  | 0  | 2  | 1    || 3 | false
        0  | 3  | 0  | 2  | 2    || 3 | false
        0  | 3  | 0  | 2  | 3    || 0 | true
        1  | 3  | 0  | 2  | 0    || 3 | false
        1  | 3  | 0  | 2  | 1    || 3 | false
        5  | 7  | 4  | 5  | 1    || 0 | true
        5  | 7  | 4  | 5  | 4    || 7 | false
        5  | 7  | 3  | 5  | 4    || 7 | false
        3  | 7  | 4  | 6  | 3    || 7 | false
        3  | 7  | 4  | 6  | 4    || 7 | false
        3  | 7  | 4  | 6  | 5    || 7 | false
        3  | 7  | 4  | 6  | 6    || 7 | false
        4  | 6  | 3  | 7  | 3    || 7 | false
        4  | 6  | 3  | 7  | 4    || 7 | false
        4  | 6  | 3  | 7  | 5    || 7 | false
        4  | 6  | 3  | 7  | 6    || 7 | false
    }

    def "FindContiguousEndTime for time sets"() {
        given:
        def t = new Instant[9]
        def time = Instant.ofEpochMilli(1328205600000L)
        for (int i = 0; i < 9; i++) {
            t[i] = time
            time = time.plusSeconds(60)
        }

        expect:
        TimeSetUtils.findContiguousEndTime(TimeSpan.ofInstants(t[a1], t[a2]), TimeSpan.ofInstants(t[b1], t[b2]), t[from]).map(res -> res == t[c] && !empty).orElse(empty)

        where:
        a1 | a2 | b1 | b2 | from || c | empty
        1  | 2  | 3  | 5  | 0    || 0 | true
        0  | 2  | 3  | 5  | 0    || 2 | false
        0  | 2  | 3  | 5  | 1    || 2 | false
        0  | 2  | 3  | 5  | 2    || 0 | true
        0  | 2  | 3  | 5  | 3    || 5 | false
        0  | 2  | 3  | 5  | 5    || 0 | true
        1  | 2  | 3  | 5  | 7    || 0 | true
        1  | 2  | 2  | 4  | 0    || 0 | true
        1  | 2  | 2  | 4  | 1    || 4 | false
        0  | 2  | 1  | 4  | 0    || 4 | false
        0  | 2  | 1  | 4  | 1    || 4 | false
        0  | 2  | 0  | 4  | 2    || 4 | false
        0  | 2  | 0  | 4  | 3    || 4 | false
        0  | 3  | 0  | 2  | 0    || 3 | false
        0  | 3  | 0  | 2  | 1    || 3 | false
        0  | 3  | 0  | 2  | 2    || 3 | false
        0  | 3  | 0  | 2  | 3    || 0 | true
        1  | 3  | 0  | 2  | 0    || 3 | false
        1  | 3  | 0  | 2  | 1    || 3 | false
        5  | 7  | 4  | 5  | 1    || 0 | true
        5  | 7  | 4  | 5  | 4    || 7 | false
        5  | 7  | 3  | 5  | 4    || 7 | false
        3  | 7  | 4  | 6  | 3    || 7 | false
        3  | 7  | 4  | 6  | 4    || 7 | false
        3  | 7  | 4  | 6  | 5    || 7 | false
        3  | 7  | 4  | 6  | 6    || 7 | false
        4  | 6  | 3  | 7  | 3    || 7 | false
        4  | 6  | 3  | 7  | 4    || 7 | false
        4  | 6  | 3  | 7  | 5    || 7 | false
        4  | 6  | 3  | 7  | 6    || 7 | false
    }

    def "findNextEmptyTime for time sets"() {
        given:
        def t = new Instant[9]
        def time = Instant.ofEpochMilli(1328205600000L)
        for (int i = 0; i < 9; i++) {
            t[i] = time
            time = time.plusSeconds(60)
        }
        def d = new Duration[9]
        for (int i = 0; i < 9; i++) {
            d[i] = Duration.between(t[0], t[i])
        }

        expect:
        TimeSetUtils.findNextEmptyTime(t[from], new DurationTimeSet(t[aAnchor], d[aBetween], d[aOn]), new DurationTimeSet(t[bAnchor], d[bBetween], d[bOn])) == t[c]

        where:
        aAnchor | aBetween | aOn | bAnchor | bBetween | bOn | from || c
        2       | 2        | 1   | 4       | 2        | 1   | 0    || 1
        2       | 2        | 1   | 4       | 2        | 1   | 1    || 1
        2       | 2        | 1   | 4       | 2        | 1   | 2    || 3
        2       | 2        | 1   | 4       | 2        | 1   | 3    || 3
        2       | 2        | 1   | 4       | 2        | 1   | 4    || 5
        2       | 2        | 1   | 4       | 2        | 1   | 5    || 5
        2       | 2        | 1   | 4       | 2        | 1   | 6    || 7
        2       | 2        | 1   | 4       | 2        | 1   | 7    || 7
        0       | 4        | 1   | 1       | 4        | 1   | 0    || 2
        0       | 4        | 1   | 1       | 4        | 1   | 1    || 2
        0       | 4        | 1   | 1       | 4        | 1   | 2    || 2
        0       | 4        | 1   | 1       | 4        | 1   | 3    || 3
        0       | 4        | 1   | 1       | 4        | 1   | 4    || 6
        0       | 4        | 1   | 1       | 4        | 1   | 5    || 6
        0       | 4        | 1   | 1       | 4        | 1   | 6    || 6
        0       | 4        | 1   | 1       | 4        | 1   | 7    || 7
        2       | 4        | 1   | 4       | 4        | 1   | 0    || 1
        2       | 4        | 1   | 4       | 4        | 1   | 1    || 1
        2       | 4        | 1   | 4       | 4        | 1   | 2    || 3
        2       | 4        | 1   | 4       | 4        | 1   | 3    || 3
        2       | 4        | 1   | 4       | 4        | 1   | 4    || 5
        2       | 4        | 1   | 4       | 4        | 1   | 5    || 5
        2       | 4        | 1   | 4       | 4        | 1   | 6    || 7
        2       | 4        | 1   | 4       | 4        | 1   | 7    || 7
        1       | 4        | 2   | 2       | 4        | 2   | 0    || 0
        1       | 4        | 2   | 2       | 4        | 2   | 1    || 4
        1       | 4        | 2   | 2       | 4        | 2   | 2    || 4
        1       | 4        | 2   | 2       | 4        | 2   | 3    || 4
        1       | 4        | 2   | 2       | 4        | 2   | 4    || 4
        1       | 4        | 2   | 2       | 4        | 2   | 5    || 8
        1       | 4        | 2   | 2       | 4        | 2   | 6    || 8
        1       | 4        | 2   | 2       | 4        | 2   | 7    || 8
    }
}
