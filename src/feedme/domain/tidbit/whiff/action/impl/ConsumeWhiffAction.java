package feedme.domain.tidbit.whiff.action.impl;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.whiff.action.WhiffAction;
import feedme.domain.tidbit.whiff.action.WhiffActionHelpers;

public record ConsumeWhiffAction() implements WhiffAction {
    @Override
    public boolean isApplicableTo(Tidbit tidbit) {
        return WhiffActionHelpers.isValidEmittedWhiff()
    }

    @Override
    public Tidbit apply(Tidbit tidbit) {
        return null;
    }
}
