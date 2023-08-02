package feedme.domain.tidbit.action.impl;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.TidbitHistory;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.action.TidbitAction;

import java.time.Instant;
import java.util.List;

public record ScheduleTaskWhiff(Instant occurredAt) implements TidbitAction {
    public ScheduleTaskWhiff() {
        this(Instant.now());
    }

    @Override
    public boolean isApplicableTo(Tidbit tidbit) {
        return tidbit.currentState() != TidbitState.Consumed
            && tidbit.whiffs().size() >= 1;
    }

    @Override
    public Tidbit apply(Tidbit tidbit) {
        List<TidbitAction> newHistory = tidbit.history().actions();
        newHistory.add(new ConsumeTidbit(Instant.now()));
        return new Tidbit(tidbit.createdAt(), TidbitState.Consumed, new TidbitHistory(newHistory), tidbit.message(), tidbit.whiffs(), tidbit.plan());
    }
}
