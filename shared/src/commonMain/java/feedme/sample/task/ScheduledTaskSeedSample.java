package feedme.sample.task;

import feedme.domain.schedule.LayeredStateSchedule;
import feedme.domain.schedule.Schedule;
import feedme.domain.schedule.timeset.TimeOfDayTimeSet;
import feedme.domain.schedule.timeset.builtin.BuiltinTimeSet;
import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.seed.SeedRepository;
import feedme.domain.tidbit.task.*;
import feedme.sample.Populator;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ScheduledTaskSeedSample implements Populator<SeedRepository> {

    private static final Schedule<TaskScheduleState> HOME_AVAILABILITY =
        LayeredStateSchedule.createWithBaseState(TaskScheduleState.Unavailable)
            .add(
                TaskScheduleState.Available,
                BuiltinTimeSet.WEEKDAYS.intersectWith(
                    new TimeOfDayTimeSet(ZoneId.systemDefault(), LocalTime.of(8, 0, 0), LocalTime.of(9, 0, 0))
                )
            )
            .add(
                TaskScheduleState.Available,
                BuiltinTimeSet.WEEKENDS.intersectWith(
                    new TimeOfDayTimeSet(ZoneId.systemDefault(), LocalTime.of(9, 0, 0), LocalTime.of(22, 0, 0))
                )
            )
            .build();

    private static final Map<Integer, Function<TidbitRepository, ScheduledTaskSeed>> SAMPLES = new HashMap<>();
    static {
        SAMPLES.put(1, (repository) -> new ScheduledTaskSeed(
            repository,
            "pack lunch",
            Instant.now().plus(24, ChronoUnit.HOURS),
            TaskPriority.Major,
            HOME_AVAILABILITY,
            Duration.ofMinutes(10)
        ));
        SAMPLES.put(2, (repository) -> new ScheduledTaskSeed(
            repository,
            "do laundry",
            Instant.now().plus(24, ChronoUnit.HOURS),
            TaskPriority.Minor,
            HOME_AVAILABILITY,
            Duration.ofMinutes(15)
        ));
    }

    private final ScheduledTaskSeed seed;

    ScheduledTaskSeedSample(ScheduledTaskSeed seed){
        this.seed = seed;
    }

    @Override
    public void populate(SeedRepository repository) {
        repository.addSeed(seed);
    }

    public static ScheduledTaskSeedSample getSample(int index, TidbitRepository tidbitRepository) {
        System.out.printf("Getting sample %d%n", index);
        return new ScheduledTaskSeedSample(SAMPLES.get(index).apply(tidbitRepository));
    }
}
