package feedme.domain.schedule.timeset.builtin;

import feedme.domain.schedule.LayeredStateSchedule;
import feedme.domain.schedule.Schedule;

public class BuiltinSchedule {
    public static final Schedule<WorkingState> Working = LayeredStateSchedule
        .createWithBaseState(WorkingState.NotWorking)
        .add(WorkingState.Working, BuiltinTimeSet.WEEKDAYS)
        .build();
    public static final Schedule<AwakeState> Awake = LayeredStateSchedule
        .createWithBaseState(AwakeState.Asleep)
        .add(AwakeState.Awake, BuiltinTimeSet.WEEKDAYS)
        .build();

    public enum WorkingState {
        Working,
        NotWorking
    }
    public enum AwakeState {
        Awake,
        Asleep;
    }
}
