package feedme.domain.tidbit.seed.impl;

import feedme.domain.tidbit.TaskTidbit;
import feedme.domain.tidbit.TidbitHistory;
import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.action.TidbitActionException;
import feedme.domain.tidbit.action.impl.EmitAction;
import feedme.domain.tidbit.action.impl.ExpireAction;
import feedme.domain.tidbit.seed.Seed;
import feedme.domain.tidbit.seed.SeedState;
import feedme.util.Fsm;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Create tasks and pester the user to do them. After the expiration date, give up.
 */
public class TaskSeed extends Seed {
    private final TidbitRepository repository;

    public final String instruction;
    public final Instant expiresAt;

    // parts of state
    @NotNull
    SeedState currentState = SeedState.New;
    TidbitRef nextScheduledTidbit;
    TidbitRef currentlyEmittedTidbit;

    private final Fsm<SeedState, TaskSeedWithTime> stateMachine;

    public TaskSeed(String instruction, Instant expiresAt, TidbitRepository repository, Duration expirationTime, Duration onItTime) {
        this.instruction = instruction;
        this.expiresAt = expiresAt;
        this.repository = repository;
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
                    TaskSeed seed = input.seed();
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
                        TaskSeed seed = input.seed();
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
                        TaskSeed seed = input.seed();
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
                        TaskSeed seed = input.seed();
                        try {
                            seed.repository.applyActionToTidbit(seed.currentlyEmittedTidbit.id(), new ExpireAction(), input.time(), TaskTidbit.class);
                            seed.currentlyEmittedTidbit = new TidbitRef(seed.addTidbit(input.time(), seed.instruction), input.time());
                            seed.repository.applyActionToTidbit(seed.currentlyEmittedTidbit.id(), new EmitAction(), input.time(), TaskTidbit.class);
                        } catch (TidbitActionException e) {
                            throw new RuntimeException(e);
                        }
                        seed.currentState = State.TidbitEmitted;
                    })
            .whenAt(State.TidbitOnIt)
                .when((input) -> input.time().isAfter(input.seed().currentlyEmittedTidbit.time().plus(onItTime)))
                    .then((input) -> {
                        // tidbit expired
                        TaskSeed seed = input.seed();
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
            this
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

    private record TaskSeedWithTime(TaskSeed seed, Instant time) {}

    private record TidbitRef(int id, Instant time) {}

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
