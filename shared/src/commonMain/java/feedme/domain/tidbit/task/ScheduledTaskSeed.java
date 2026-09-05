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
import feedme.domain.tidbit.action.impl.ExpireAction;
import feedme.domain.tidbit.plan.impl.ScheduledTidbitPlan;
import feedme.domain.tidbit.urgency.BuiltinUrgency;
import feedme.util.InfInstant;
import feedme.util.TimeUtils;
import java.time.*;
import org.jetbrains.annotations.NotNull;

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
        TaskPriority priority,
        Schedule<TaskScheduleState> schedule,
        Duration taskDuration
	) {
        super(repository, instruction, expiresAt, priority);
        this.schedule = schedule;
        this.tidbitScheduledFor = new TidbitScheduledFor(this).withRepository(repository);
		this.plan = createPlan(schedule, expiresAt, priority, taskDuration);
    }

    private static ScheduledTidbitPlan createPlan(Schedule<TaskScheduleState> schedule, Instant expiresAt, TaskPriority priority, Duration taskDuration) {
        Duration minPeriod = calculateMinPeriod(priority, taskDuration);
        long tidbitCount = calculateTidbitCount(priority);
        SortedSet<Instant> scheduledTimes = new TreeSet<>();
        TimeSet availableTimes = schedule.getTimeSetFor(TaskScheduleState.Available).intersectWith(TimeSpan.withUpperBound(expiresAt));
        Instant currentTime = expiresAt.minus(taskDuration);
        while (scheduledTimes.size() < tidbitCount) {
            if (availableTimes.contains(currentTime)) {
                scheduledTimes.add(currentTime);
                currentTime = currentTime.minus(minPeriod);
            } else {
                final Instant checkTime;
                // We want our last tidbit to be <taskDuration> before last chance
                if (scheduledTimes.isEmpty()) {
                    checkTime = currentTime.minus(taskDuration);
                } else {
                    checkTime = currentTime.minus(minPeriod);
                }
                currentTime = TimeUtils.earliest(
                    availableTimes
                        .getPreviousInclusive(checkTime)
                        .map(TimeSpan::end)
                        .orElseThrow(),
                    InfInstant.of(checkTime)
                ).getInstantOrElseThrow();
            }
        }
        return new ScheduledTidbitPlan(scheduledTimes);
    }

    private static Duration calculateMinPeriod(TaskPriority priority, Duration taskDuration) {
        return taskDuration.multipliedBy(1);
    }

    private static long calculateTidbitCount(TaskPriority priority) {
        return switch (priority) {
            case Critical -> 5L;
            case Major -> 3L;
            case Minor -> 1L;
        };
    }

    @Override
    public void updateTidbits(@NotNull Instant now) {
        // If this seed is expired, we can just expire all tidbits
        if (!now.isBefore(expiresAt)) {
            if (repository.getTidbitsForSeed(this, TaskTidbit.class).values().stream().anyMatch(tidbit -> !TidbitState.Expired.equals(tidbit.currentState))) {
                expireAllTidbits(now);
            }
		}
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

    @Override
    public String toString() {
        return new StringJoiner(", ", ScheduledTaskSeed.class.getSimpleName() + "[", "]")
            .add("instruction=" + instruction)
            .add("expiresAt=" + expiresAt.atZone(ZoneId.systemDefault()))
            .add("tidbitCount=" + plan.tidbitCount())
            .toString();
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

    private void emitTidbit(@NotNull Instant now, int tidbitId, ScheduledTaskTidbit tidbit) {
        if (!tidbit.currentState.isFinished() && !tidbit.currentState.isVisible()) {
            try {
                expireAllTidbits(now);
                repository.applyActionToTidbit(tidbitId, new EmitAction(), now, ScheduledTaskTidbit.class);
            } catch (TidbitActionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void expireAllTidbits(@NotNull Instant now) {
        for (Map.Entry<Integer, TaskTidbit> entry : repository.getTidbitsForSeed(this, TaskTidbit.class).entrySet()) {
            try {
                repository.applyActionToTidbit(entry.getKey(), new ExpireAction(), now, TaskTidbit.class);
            } catch (TidbitActionException e) {
                throw new RuntimeException(e);
            }
        }
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
