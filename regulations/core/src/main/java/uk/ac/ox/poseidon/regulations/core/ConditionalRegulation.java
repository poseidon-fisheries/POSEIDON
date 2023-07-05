package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Mode;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.function.Predicate;

public class ConditionalRegulation implements Regulation {

    private final Predicate<? super Action> condition;
    private final Regulation regulationIfTrue;
    private final Regulation regulationIfFalse;

    public ConditionalRegulation(
        final Predicate<? super Action> condition,
        final Regulation regulationIfTrue,
        final Regulation regulationIfFalse
    ) {
        this.condition = condition;
        this.regulationIfTrue = regulationIfTrue;
        this.regulationIfFalse = regulationIfFalse;
    }

    @Override
    public Mode mode(final Action action) {
        final Regulation regulation =
            condition.test(action) ?
                regulationIfTrue :
                regulationIfFalse;
        return regulation.mode(action);
    }

}
