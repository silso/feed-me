package feedme.domain.tidbit.whiff.action;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.whiff.action.impl.ConsumeWhiffAction;
import feedme.domain.tidbit.whiff.action.impl.OnItWhiffAction;

import java.util.function.Supplier;

public interface WhiffAction {
    boolean isApplicableTo(Tidbit tidbit);
    Tidbit apply(Tidbit tidbit);

    enum Impl {
        OnItWhiffAction(OnItWhiffAction::new),
        ConsumeWhiffAction(ConsumeWhiffAction::new);

        private final Supplier<WhiffAction> supplier;
        Impl(Supplier<WhiffAction> supplier) {
            this.supplier = supplier;
        }

        public WhiffAction create() {
            return supplier.get();
        }
    }
}
