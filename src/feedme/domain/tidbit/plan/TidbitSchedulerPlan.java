package feedme.domain.tidbit.plan;

import java.time.Duration;
import java.time.Instant;

public interface TidbitSchedulerPlan {
    Instant getNext(Instant currentTime, Duration timeRemaining);
}
