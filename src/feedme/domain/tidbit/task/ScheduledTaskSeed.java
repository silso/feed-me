package feedme.domain.tidbit.task;

import feedme.domain.repository.DerivedWithArgument;
import feedme.domain.schedule.Schedule;
import feedme.domain.schedule.timeset.TimeSet;
import feedme.domain.schedule.timeset.TimeSpan;
import feedme.domain.tidbit.TidbitHistory;
import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.action.TidbitActionException;
import feedme.domain.tidbit.action.impl.EmitAction;
import feedme.domain.tidbit.plan.impl.ScheduledTidbitPlan;
import feedme.domain.tidbit.urgency.BuiltinUrgency;
import feedme.util.TimeUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
        int tidbitCount
    ) {
        super(repository, instruction, expiresAt);
        this.schedule = schedule;
        this.tidbitScheduledFor = new TidbitScheduledFor(this).withRepository(repository);
        this.plan = createPlan(schedule, tidbitCount);
    }

    private ScheduledTidbitPlan createPlan(Schedule<TaskScheduleState> schedule, int tidbitCount) {
        Duration offset = Duration.ofMinutes(1);
        Duration minPeriod = Duration.ofMinutes(5);
        SortedSet<Instant> scheduledTimes = new TreeSet<>();
        TimeSet availableTime = schedule.getTimeSetFor(TaskScheduleState.Available).unionWith(TimeSpan.ofInstants(Instant.MIN, expiresAt));
        Instant currentTime = expiresAt.minus(offset);
        while (scheduledTimes.size() < tidbitCount) {
            if (availableTime.contains(currentTime)) {
                scheduledTimes.add(currentTime);
                currentTime = currentTime.minus(minPeriod);
            } else {
                Instant checkTime = currentTime.minus(minPeriod);
                currentTime = TimeUtils.earliest(availableTime.getPreviousInclusive(checkTime).map(TimeSpan::lastTime).orElseThrow(), checkTime);
            }
        }
        return new ScheduledTidbitPlan(scheduledTimes);
    }

    @Override
    public void createTidbits(@NotNull Instant now) {
        // if the previous tidbit is not emitted or finished, emit, if it doesn't exist, create and emit
        plan
            .getPrev(now, Duration.between(now, expiresAt))
            .ifPresent(previousTime -> {
                tidbitScheduledFor.apply(previousTime).ifPresentOrElse(
                    entry -> {
                        int tidbitId = entry.getKey();
                        ScheduledTaskTidbit tidbit = entry.getValue();
                        emitTidbit(now, tidbitId, tidbit);
                    },
                    () -> {
                        addTidbit(now, previousTime);
                        Map.Entry<Integer, ScheduledTaskTidbit> entry = tidbitScheduledFor.apply(previousTime).orElseThrow();
                        emitTidbit(previousTime, entry.getKey(), entry.getValue());
                    }
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

    private void emitTidbit(@NotNull Instant now, int tidbitId, ScheduledTaskTidbit tidbit) {
        if (!tidbit.currentState.isFinished() && !tidbit.currentState.isVisible()) {
            try {
                repository.applyActionToTidbit(tidbitId, new EmitAction(), now, ScheduledTaskTidbit.class);
            } catch (TidbitActionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int addTidbit(Instant createdAt, Instant scheduledFor) {
        return repository.addTidbit(new ScheduledTaskTidbit(
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
