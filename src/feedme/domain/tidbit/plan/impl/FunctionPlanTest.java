package feedme.domain.tidbit.plan.impl;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FunctionPlanTest {

    @Test
    void testInverseFunctionIsActuallyInverse() {
        FunctionPlan plan = FunctionPlan.exponential(
            2.1D, 10D, Duration.ofDays(2)
        );
        Arrays.asList(0.1D, 1.0D, 1.5D, 5.0D, 9.99D).forEach(num -> {
            Duration duration = plan.inverseGetWhiffNum().apply(num);
            System.out.println(duration);
            assertEquals(num, plan.getWhiffNum().apply(duration), num * 0.00001f);
        });

        Arrays.asList(Duration.ofHours(100), Duration.ofHours(48), Duration.ofHours(1), Duration.ofMinutes(5), Duration.ofMillis(500)).forEach(timeRemaining -> {
            double whiffNum = plan.getWhiffNum().apply(timeRemaining);
            System.out.println(whiffNum);
            assertEquals(timeRemaining.toMillis(), plan.inverseGetWhiffNum().apply(whiffNum).toMillis(), timeRemaining.toMillis() * 0.00001f);
        });
    }
}