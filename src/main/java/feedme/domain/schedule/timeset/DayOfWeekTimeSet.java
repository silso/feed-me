package feedme.domain.schedule.timeset;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.jetbrains.annotations.NotNull;

public class DayOfWeekTimeSet extends DiscretePeriodicTimeSet<DiscretePeriodicTimeSet.DayInstant> {

    private final TimeZone timeZone;
    private final Map<DayOfWeek, Long> countdownSinceMap = new HashMap<>();
    private final Map<DayOfWeek, Long> countdownUntilMap = new HashMap<>();

    public DayOfWeekTimeSet(@NotNull TimeZone timeZone, @NotNull Set<DayOfWeek> days) {
        this.timeZone = timeZone;
        createCountdownFunction(days);
    }

    private DayOfWeek indexToDay(int i) {
        return DayOfWeek.of(Math.floorMod(i, 7) + 1);
    }

    private void createCountdownFunction(Set<DayOfWeek> days) {
        if (days.isEmpty()) {
            throw new IllegalArgumentException("Can't create TimeSet for no days of the week");
        }
        if (days.size() == 7) {
            throw new IllegalArgumentException("Can't create TimeSet for all days of the week");
        }

        for (int i = 0; i < 14; i++) {
            DayOfWeek yesterday = indexToDay(i - 1);
            DayOfWeek today = indexToDay(i);
            if (days.contains(today)) {
                if (!days.contains(yesterday)) {
                    countdownSinceMap.put(today, 0L);
                } else {
                    if (countdownSinceMap.containsKey(yesterday)) {
                        countdownSinceMap.put(today, countdownSinceMap.get(yesterday) - 1);
                    }
                }
            } else {
                if (days.contains(yesterday)) {
                    countdownSinceMap.put(today, 1L);
                } else {
                    if (countdownSinceMap.containsKey(yesterday)) {
                        countdownSinceMap.put(today, countdownSinceMap.get(yesterday) + 1);
                    }
                }
            }
        }

        for (int i = 13; i >= 0; i--) {
            DayOfWeek today = indexToDay(i);
            DayOfWeek tomorrow = indexToDay(i + 1);
            if (days.contains(today)) {
                if (!days.contains(tomorrow)) {
                    countdownUntilMap.put(today, 0L);
                } else {
                    if (countdownUntilMap.containsKey(tomorrow)) {
                        countdownUntilMap.put(today, countdownUntilMap.get(tomorrow) - 1);
                    }
                }
            } else {
                if (days.contains(tomorrow)) {
                    countdownUntilMap.put(today, 1L);
                } else {
                    if (countdownUntilMap.containsKey(tomorrow)) {
                        countdownUntilMap.put(today, countdownUntilMap.get(tomorrow) + 1);
                    }
                }
            }
        }
    }

    @Override
    protected long stepsSince(DayInstant time) {
        return countdownSinceMap.get(time.getDayOfWeek());
    }

    @Override
    protected long stepsUntil(DayInstant time) {
        return countdownUntilMap.get(time.getDayOfWeek());
    }

    @Override
    protected DayInstant toDiscrete(Instant continuousTime) {
        return new DayInstant(timeZone, continuousTime);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
