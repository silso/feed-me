package feedme.domain.schedule.timeset;

import java.time.Instant;
import java.util.Optional;

public interface Periodic {
    Instant getPrevious(Instant time);
    Optional<Instant> getAt(Instant time);
    Instant getNext(Instant time);
}
