package feedme.domain.tidbit.plan.impl;

import feedme.domain.tidbit.plan.TidbitSchedulerPlan;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.SortedSet;

public class ScheduledTidbitPlan implements TidbitSchedulerPlan {
    private final SortedSet<Instant> scheduledTidbits;

    public ScheduledTidbitPlan(SortedSet<Instant> scheduledTidbits) {
        this.scheduledTidbits = scheduledTidbits;
    }

    @Override
    public Optional<Instant> getPrev(Instant currentTime, Duration timeRemaining) {
        SortedSet<Instant> headSet = scheduledTidbits.headSet(currentTime);
        return headSet.isEmpty() ? Optional.empty() : Optional.of(headSet.last());
    }

    @Override
    public Optional<Instant> getNext(Instant currentTime, Duration timeRemaining) {
        SortedSet<Instant> tailSet = scheduledTidbits.tailSet(currentTime);
        return tailSet.isEmpty() ? Optional.empty() : Optional.of(tailSet.first());
    }
}
