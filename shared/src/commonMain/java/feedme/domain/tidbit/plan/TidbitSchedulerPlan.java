package feedme.domain.tidbit.plan;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface TidbitSchedulerPlan {
    Optional<Instant> getPrev(Instant currentTime, Duration timeRemaining);
    Optional<Instant> getNext(Instant currentTime, Duration timeRemaining);
}
