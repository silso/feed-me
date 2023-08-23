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

    public TaskSeed(String instruction, Instant expiresAt, TidbitRepository repository) {
        this.instruction = instruction;
        this.expiresAt = expiresAt;
        this.repository = repository;
        this.stateMachine = createStateMachine(
            expiresAt.minus(Duration.ofHours(1)),
            Duration.ofMinutes(5),
            Duration.ofMinutes(10)
        );
    }

    private Fsm<SeedState, TaskSeedWithTime> createStateMachine(
        Instant firstTidbitTime,
        Duration tidbitExpirationTime,
        Duration onItTime
    ) {
        return Fsm.<SeedState, TaskSeedWithTime>create()
            .whenAt(State.New)
                .always((input) -> {
                    TaskSeed seed = input.seed();
                    seed.nextScheduledTidbit = new TidbitRef(seed.addTidbit(input.time(), seed.instruction), firstTidbitTime);
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
                .when((input) -> input.time().isAfter(input.seed().currentlyEmittedTidbit.time().plus(tidbitExpirationTime)))
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
        this.stateMachine.step(currentState, new TaskSeedWithTime(this, now));
    }

    private record TaskSeedWithTime(TaskSeed seed, Instant time) {}

    private record TidbitRef(int id, Instant time) {}

    public static class State extends SeedState {
        public static final State TidbitScheduled = new State("A");
        public static final State TidbitEmitted = new State("B");
        public static final State TidbitOnIt = new State("C");
        public static final State TidbitConsumed = new State("D");
        public static final State SeedExpired = new State("E");

        protected State(@NotNull String name) {
            super(name);
        }
    }
}
