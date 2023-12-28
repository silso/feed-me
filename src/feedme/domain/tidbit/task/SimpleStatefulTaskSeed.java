package feedme.domain.tidbit.task;

import com.google.common.base.Objects;
import feedme.domain.tidbit.TidbitHistory;
import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.action.TidbitActionException;
import feedme.domain.tidbit.action.impl.EmitAction;
import feedme.domain.tidbit.action.impl.ExpireAction;
import feedme.domain.tidbit.seed.SeedState;
import feedme.domain.tidbit.urgency.BuiltinUrgency;
import feedme.util.Fsm;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Create tasks and pester the user to do them. After the expiration date, give up.
 */
public class SimpleStatefulTaskSeed extends TaskSeed {

    // parts of state
    @NotNull
    SeedState currentState = SeedState.New;
    TidbitRef nextScheduledTidbit;
    TidbitRef currentlyEmittedTidbit;

    private final Fsm<SeedState, TaskSeedWithTime> stateMachine;

    public SimpleStatefulTaskSeed(String instruction, Instant expiresAt, TidbitRepository repository, Duration expirationTime, Duration onItTime) {
        super(repository, instruction, expiresAt);
        this.stateMachine = createStateMachine(
            expiresAt.minus(Duration.ofHours(1)),
            expirationTime,
            onItTime
        );
    }

    private Fsm<SeedState, TaskSeedWithTime> createStateMachine(
        Instant firstTidbitTime,
        Duration expirationTime,
        Duration onItTime
    ) {
        return Fsm.<SeedState, TaskSeedWithTime>create()
            .whenAt(State.New)
                .always((input) -> {
                    SimpleStatefulTaskSeed seed = input.seed();
                    Instant scheduledTime = firstTidbitTime;
                    if (input.time().isAfter(firstTidbitTime)) {
                        scheduledTime = input.time();
                    }
                    seed.nextScheduledTidbit = new TidbitRef(seed.addTidbit(input.time(), seed.instruction), scheduledTime);
                    seed.currentState = State.TidbitScheduled;
                })
            .whenAt(State.TidbitScheduled)
                .when((input) -> input.time().isAfter(input.seed().nextScheduledTidbit.time()))
                    .then((input) -> {
                        // emit tidbit
                        SimpleStatefulTaskSeed seed = input.seed();
                        try {
                            seed.repository.applyActionToTidbit(seed.nextScheduledTidbit.id(), new EmitAction(), input.time(), TaskTidbit.class);
                        } catch (TidbitActionException e) {
                            throw new RuntimeException(e);
                        }
                        seed.currentlyEmittedTidbit = seed.nextScheduledTidbit;
                        seed.currentState = State.TidbitEmitted;
                    })
            .whenAt(State.TidbitEmitted)
                .when((input) -> input.time().isAfter(input.seed().expiresAt))
                    .then((input) -> {
                        // seed expired
                        SimpleStatefulTaskSeed seed = input.seed();
                        try {
                            seed.repository.applyActionToTidbit(seed.currentlyEmittedTidbit.id(), new ExpireAction(), input.time(), TaskTidbit.class);
                        } catch (TidbitActionException e) {
                            throw new RuntimeException(e);
                        }
                        seed.currentState = State.SeedExpired;
                    })
                .when((input) -> input.time().isAfter(input.seed().currentlyEmittedTidbit.time().plus(expirationTime)))
                    .then((input) -> {
                        // tidbit expired
                        SimpleStatefulTaskSeed seed = input.seed();
                        try {
                            seed.repository.applyActionToTidbit(seed.currentlyEmittedTidbit.id(), new ExpireAction(), input.time(), TaskTidbit.class);
                            seed.currentlyEmittedTidbit = new TidbitRef(seed.addTidbit(input.time(), seed.instruction), input.time());
                            seed.repository.applyActionToTidbit(seed.currentlyEmittedTidbit.id(), new EmitAction(), input.time(), TaskTidbit.class);
                        } catch (TidbitActionException e) {
                            throw new RuntimeException(e);
                        }
                        seed.currentState = State.TidbitEmitted;
                    })
                .when((input) -> TaskTidbit.State.OnIt.equals(repository.getTidbit(input.seed().currentlyEmittedTidbit.id).orElseThrow().currentState))
                    .then((input) -> input.seed().currentState = State.TidbitOnIt)
                .when((input) -> TaskTidbit.State.Consumed.equals(repository.getTidbit(input.seed().currentlyEmittedTidbit.id).orElseThrow().currentState))
                    .then((input) -> input.seed().currentState = State.TidbitConsumed)
            .whenAt(State.TidbitOnIt)
                .when((input) -> input.time().isAfter(input.seed().currentlyEmittedTidbit.time().plus(onItTime)))
                    .then((input) -> {
                        // tidbit expired
                        SimpleStatefulTaskSeed seed = input.seed();
                        try {
                            seed.repository.applyActionToTidbit(seed.currentlyEmittedTidbit.id(), new ExpireAction(), input.time(), TaskTidbit.class);
                            seed.currentlyEmittedTidbit = new TidbitRef(seed.addTidbit(input.time(), seed.instruction), input.time());
                            seed.repository.applyActionToTidbit(seed.currentlyEmittedTidbit.id(), new EmitAction(), input.time(), TaskTidbit.class);
                        } catch (TidbitActionException e) {
                            throw new RuntimeException(e);
                        }
                        seed.currentState = State.TidbitEmitted;
                    })
            .build();
    }

    private int addTidbit(Instant now, String instruction) {
        return repository.addTidbit(new TaskTidbit(
            now,
            TaskTidbit.State.New,
            new TidbitHistory(new ArrayList<>()),
            instruction,
            this,
            BuiltinUrgency.Push.urgency
        ));
    }

    /**
     * @param now the time to create tidbits for
     */
    @Override
    public void createTidbits(@NotNull Instant now) {
        if (!State.isFinished(currentState)) {
            this.stateMachine.step(currentState, new TaskSeedWithTime(this, now));
        }
    }

//
//    public static TaskSeed fromInputs1(String instruction, Instant expiresAt, TaskPriority priority, Duration taskTime, Schedule<TaskScheduleState> schedule, TidbitRepository tidbitRepository, Duration lastTidbitBuffer, Duration firstTidbitBuffer) {
//        int tidbitCount = switch (priority) {
//            case Critical -> 10;
//            case Major -> 5;
//            case Minor -> 3;
//        };
//        Instant lastTidbitTime = expiresAt.minus(lastTidbitBuffer);
//        Duration expirationTime = taskTime.plus(TimeUtils.multiply(taskTime, switch (priority) {
//            case Critical -> 1.5;
//            case Major -> 1.2;
//            case Minor -> 1.0;
//        }));
//        return new TaskSeed(instruction, expiresAt, tidbitRepository);
//    }

    private record TaskSeedWithTime(SimpleStatefulTaskSeed seed, Instant time) {}

    private record TidbitRef(int id, Instant time) {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleStatefulTaskSeed seed)) return false;
        return Objects.equal(repository, seed.repository) && Objects.equal(instruction, seed.instruction) && Objects.equal(expiresAt, seed.expiresAt) && Objects.equal(currentState, seed.currentState) && Objects.equal(nextScheduledTidbit, seed.nextScheduledTidbit) && Objects.equal(currentlyEmittedTidbit, seed.currentlyEmittedTidbit) && Objects.equal(stateMachine, seed.stateMachine);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(repository, instruction, expiresAt, currentState, nextScheduledTidbit, currentlyEmittedTidbit, stateMachine);
    }

    @Override
    public String toString() {
        return "TaskSeed{" +
            "instruction='" + instruction + '\'' +
            ", expiresAt=" + expiresAt +
            ", currentState=" + currentState +
            '}';
    }

    public static class State extends SeedState {
        public static final State TidbitScheduled = new State("TidbitScheduled");
        public static final State TidbitEmitted = new State("TidbitEmitted");
        public static final State TidbitOnIt = new State("TidbitOnIt");
        public static final State TidbitConsumed = new State("TidbitConsumed");
        public static final State SeedExpired = new State("SeedExpired");

        protected State(@NotNull String name) {
            super(name);
        }

        public static boolean isFinished(SeedState state) {
            return TidbitConsumed.equals(state) || SeedExpired.equals(state);
        }
    }
}
