package feedme.domain.schedule.timeset

import feedme.domain.schedule.timeset.DurationTimeSet
import feedme.domain.schedule.timeset.TimeSet
import feedme.domain.schedule.timeset.TimeSpan
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class DurationTimeSetTest extends Specification {
    def "getPrevious set A"() {
        given:
        TimeSet set = new DurationTimeSet(Instant.EPOCH, Duration.ofSeconds(20), Duration.ofSeconds(10))

        expect:
        set.getPrevious(Instant.EPOCH.plusSeconds(a)).orElseThrow() == TimeSpan.ofInstants(Instant.EPOCH.plusSeconds(b1), Instant.EPOCH.plusSeconds(b2))

        where:
          a ||  b1 |  b2
        -21 || -40 | -30
        -15 || -40 | -30
        -10 || -20 | -10
         -1 || -20 | -10
          0 || -20 | -10
          1 || -20 | -10
         10 ||   0 |  10
         11 ||   0 |  10
         20 ||   0 |  10
         25 ||   0 |  10
         30 ||  20 |  30
    }

    def "getAt set A"() {
        given:
        TimeSet set = new DurationTimeSet(Instant.EPOCH, Duration.ofSeconds(20), Duration.ofSeconds(10))

        expect:
        set.getAt(Instant.EPOCH.plusSeconds(a)).map(res -> res == TimeSpan.ofInstants(Instant.EPOCH.plusSeconds(b1), Instant.EPOCH.plusSeconds(b2)) && !empty).orElse(empty)

        where:
          a ||  b1 |  b2 | empty
        -10 || -20 | -10 | true
         -1 || -20 | -10 | true
          0 ||   0 |  10 | false
          1 ||   0 |  10 | false
         10 ||   0 |  10 | true
         11 ||   0 |  10 | true
         20 ||  20 |  30 | false
         25 ||  20 |  30 | false
         30 ||  20 |  30 | true
    }

    def "getNext set A"() {
        given:
        TimeSet set = new DurationTimeSet(Instant.EPOCH, Duration.ofSeconds(20), Duration.ofSeconds(10))

        expect:
        set.getNext(Instant.EPOCH.plusSeconds(a)).orElseThrow() == TimeSpan.ofInstants(Instant.EPOCH.plusSeconds(b1), Instant.EPOCH.plusSeconds(b2))

        where:
          a ||  b1 |  b2
        -21 || -20 | -10
        -15 ||   0 |  10
        -10 ||   0 |  10
         -1 ||   0 |  10
          0 ||  20 |  30
          1 ||  20 |  30
         10 ||  20 |  30
         11 ||  20 |  30
         20 ||  40 |  50
         25 ||  40 |  50
         30 ||  40 |  50
    }
    def "getPrevious set B"() {
        given:
        TimeSet set = new DurationTimeSet(Instant.EPOCH.plusSeconds(5), Duration.ofSeconds(20), Duration.ofSeconds(15))

        expect:
        set.getPrevious(Instant.EPOCH.plusSeconds(a)).orElseThrow() == TimeSpan.ofInstants(Instant.EPOCH.plusSeconds(b1), Instant.EPOCH.plusSeconds(b2))

        where:
          a ||  b1 |  b2
        -21 || -55 | -40
        -15 || -35 | -20
        -10 || -35 | -20
         -1 || -35 | -20
          0 || -15 |   0
          1 || -15 |   0
         10 || -15 |   0
         11 || -15 |   0
         20 ||   5 |  20
         25 ||   5 |  20
         30 ||   5 |  20
    }

    def "getAt set B"() {
        given:
        TimeSet set = new DurationTimeSet(Instant.EPOCH.plusSeconds(5), Duration.ofSeconds(20), Duration.ofSeconds(15))

        expect:
        set.getAt(Instant.EPOCH.plusSeconds(a)).map(res -> res == TimeSpan.ofInstants(Instant.EPOCH.plusSeconds(b1), Instant.EPOCH.plusSeconds(b2)) && !empty).orElse(empty)

        where:
          a ||  b1 |  b2 | empty
        -21 || -35 | -20 | false
        -15 || -15 |   0 | false
        -10 || -15 |   0 | false
         -1 || -15 |   0 | false
          0 ||   0 |  10 | true
          1 ||   0 |  10 | true
         10 ||   5 |  20 | false
         11 ||   5 |  20 | false
         20 ||  20 |  30 | true
         25 ||  25 |  40 | false
         30 ||  25 |  40 | false
    }

    def "getNext set B"() {
        given:
        TimeSet set = new DurationTimeSet(Instant.EPOCH.plusSeconds(5), Duration.ofSeconds(20), Duration.ofSeconds(15))

        expect:
        set.getNext(Instant.EPOCH.plusSeconds(a)).orElseThrow() == TimeSpan.ofInstants(Instant.EPOCH.plusSeconds(b1), Instant.EPOCH.plusSeconds(b2))

        where:
          a ||  b1 |  b2
        -21 || -15 |   0
        -15 ||   5 |  20
        -10 ||   5 |  20
         -1 ||   5 |  20
          0 ||   5 |  20
          1 ||   5 |  20
         10 ||  25 |  40
         11 ||  25 |  40
         20 ||  25 |  40
         25 ||  45 |  60
         30 ||  45 |  60
    }
}
