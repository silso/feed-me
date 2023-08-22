package feedme.domain.tidbit.action;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.action.impl.ConsumeAction;

import java.time.Instant;
import java.util.function.Function;

public abstract class TidbitAction {
    private Instant occurredAt;
    private boolean applied = false;

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public boolean isApplicableTo(Tidbit<?> tidbit) {
        return tidbit.availableActions().contains(this.getClass()) && doIsApplicableTo(tidbit);
    }

    public abstract boolean doIsApplicableTo(Tidbit<?> tidbit);

    public <TidbitType extends Tidbit<?>> TidbitType apply(TidbitType tidbit, Instant now) throws TidbitActionException {
        if (!isApplicableTo(tidbit)) {
            throw new TidbitActionException("Action " + this + " isn't applicable to " + tidbit);
        }
        if (applied) {
            throw new TidbitActionException("Cannot re-apply action: " + this);
        }
        occurredAt = now;
        applied = true;
        tidbit.history.actions().add(this);
        return doApply(tidbit);
    }

    public abstract <TidbitType extends Tidbit<?>> TidbitType doApply(TidbitType tidbit);
}
