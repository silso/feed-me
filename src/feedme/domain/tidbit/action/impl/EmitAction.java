package feedme.domain.tidbit.action.impl;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.action.TidbitStateTransition;

public class EmitAction extends TidbitStateTransition {
    @Override
    public boolean doIsApplicableTo(Tidbit<?> tidbit) {
        return true;
    }

    @Override
    public TidbitState toState() {
        return TidbitState.Visible;
    }
}
