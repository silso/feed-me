package feedme.domain.tidbit.whiff.action.impl;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.whiff.action.WhiffAction;

public record OnItWhiffAction() implements WhiffAction {
    @Override
    public boolean isApplicableTo(Tidbit tidbit) {
        return false;
    }

    @Override
    public Tidbit apply(Tidbit tidbit) {
        return null;
    }
}
