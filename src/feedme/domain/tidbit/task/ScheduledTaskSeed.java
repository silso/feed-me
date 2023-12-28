package feedme.domain.tidbit.task;

import feedme.domain.repository.DerivedWithArgument;
import feedme.domain.schedule.Schedule;
import feedme.domain.tidbit.TidbitHistory;
import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.action.TidbitActionException;
import feedme.domain.tidbit.action.impl.EmitAction;
import feedme.domain.tidbit.plan.impl.ScheduledTidbitPlan;
import feedme.domain.tidbit.urgency.BuiltinUrgency;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ScheduledTaskSeed extends TaskSeed {
    protected final Schedule<TaskScheduleState> schedule;
    protected final ScheduledTidbitPlan plan;

    private final Function<Instant, Optional<Map.Entry<Integer, ScheduledTaskTidbit>>> tidbitScheduledFor;

    public ScheduledTaskSeed(
        TidbitRepository repository,
        String instruction,
        Instant expiresAt,
        Schedule<TaskScheduleState> schedule,
        ScheduledTidbitPlan plan
    ) {
        super(repository, instruction, expiresAt);
        this.schedule = schedule;
        this.plan = plan;
        this.tidbitScheduledFor = new TidbitScheduledFor(this).withRepository(repository);
    }

    @Override
    public void createTidbits(@NotNull Instant now) {
        // if the previous tidbit is not emitted or finished, emit, if it doesn't exist, create and emit
        plan
            .getPrev(now, Duration.between(now, expiresAt))
            .ifPresent(previousTime -> {
                tidbitScheduledFor.apply(previousTime).ifPresentOrElse(
                    entry -> {
                        if (!entry.getValue().currentState.isFinished() && !entry.getValue().currentState.isVisible()) {
                            try {
                                repository.applyActionToTidbit(entry.getKey(), new EmitAction(), now, ScheduledTaskTidbit.class);
                            } catch (TidbitActionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    },
                    () -> addTidbit(now, previousTime)
                );
            });
        // if the next tidbit doesn't exist
        plan
            .getNext(now, Duration.between(now, expiresAt))
            .ifPresent(nextTime -> {
                tidbitScheduledFor.apply(nextTime).ifPresentOrElse(
                    entry -> {
                    },
                    () -> addTidbit(now, nextTime)
                );
            });
    }

    private void addTidbit(Instant createdAt, Instant scheduledFor) {
        repository.addTidbit(new ScheduledTaskTidbit(
            createdAt,
            TidbitState.New,
            new TidbitHistory(new ArrayList<>()),
            instruction,
            this,
            BuiltinUrgency.Push.urgency,
            scheduledFor
        ));
    }

    private record TidbitScheduledFor(ScheduledTaskSeed seed) implements DerivedWithArgument<TidbitRepository, Optional<Map.Entry<Integer, ScheduledTaskTidbit>>, Instant> {
        @Override
        public Optional<Map.Entry<Integer, ScheduledTaskTidbit>> get(TidbitRepository repository, Instant argument) {
            return repository
                .getTidbitsForSeed(seed, ScheduledTaskTidbit.class)
                .entrySet()
                .stream()
                .filter(entry -> argument.equals(entry.getValue().scheduledFor))
                .findFirst();
        }
    }
}
