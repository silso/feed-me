package feedme.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FsmTest {

    @Test
    void testSimpleStateMachine() {
        AtomicInteger state = new AtomicInteger(0);
        AtomicInteger sideState = new AtomicInteger(10);

        Fsm<Integer, Integer> fsm = Fsm.<Integer, Integer>create()
            .whenAt(0)
                .when((s) -> sideState.get() < 8)
                    .then((i) -> state.set(1))
                .otherwise(sideState::decrementAndGet)
            .whenAt(1)
                .when((s) -> sideState.get() < 8)
                    .then(() -> state.set(2))
                .when((s) -> sideState.get() > 10)
                    .then(() -> state.set(3))
                .otherwise(sideState::incrementAndGet)
            .whenAt(2)
                .always(() -> {
                    state.set(1);
                    sideState.set(9);
                })
            .whenAt(3)
                .always(() -> {
                    System.out.println("fin");
                    state.set(4);
                })
            .build();

        int steps = 0;
        while (state.get() != 4) {
            System.out.printf("At state %d with sideState %d%n", state.get(), sideState.get());
            fsm.step(state.get(), state.get());
            steps++;
        }

        assertEquals(10, steps);
        assertEquals(4, state.get());
        assertEquals(11, sideState.get());
    }
}
