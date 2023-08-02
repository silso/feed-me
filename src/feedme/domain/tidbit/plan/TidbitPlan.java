package feedme.domain.tidbit.plan;

import java.time.Duration;
import java.time.Instant;

public interface TidbitPlan {
    Instant getNextWhiff(Instant currentTime, Duration timeRemaining);
}
