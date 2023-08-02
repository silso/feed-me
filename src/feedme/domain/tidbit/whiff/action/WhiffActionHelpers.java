package feedme.domain.tidbit.whiff.action;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.whiff.Whiff;

import java.util.List;

public class WhiffActionHelpers {
    public static boolean isValidEmittedWhiff(Whiff whiff, Tidbit tidbit) {
        List<Whiff> whiffs = tidbit.whiffs();
        return whiffs.size() > 0
            && whiff.currentState() == Whiff.State.Emitted
            && whiffs.contains(whiff);
    }
}
