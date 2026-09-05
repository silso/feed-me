package feedme.domain.tidbit.task;

import feedme.domain.tidbit.TidbitHistory;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.seed.Seed;
import feedme.domain.tidbit.urgency.Urgency;

import java.time.Instant;

public class ScheduledTaskTidbit extends TaskTidbit {
    public final Instant scheduledFor;

    public ScheduledTaskTidbit(Instant createdAt, TidbitState currentState, TidbitHistory history, String message, Seed seed, Urgency urgency, Instant scheduledFor) {
        super(createdAt, currentState, history, message, seed, urgency);
        this.scheduledFor = scheduledFor;
    }
}
