package feedme.domain.tidbit.action;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.action.impl.ConsumeTidbit;
import feedme.domain.tidbit.action.impl.InhaleTaskWhiff;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TidbitAction {
    Instant occurredAt();
    boolean isApplicableTo(Tidbit tidbit);
    Tidbit apply(Tidbit tidbit);

    class Impl<ActionType extends TidbitAction> {
        public static final Function<Instant, ConsumeTidbit> ConsumeTidbit = ConsumeTidbit::new;
    }
}
