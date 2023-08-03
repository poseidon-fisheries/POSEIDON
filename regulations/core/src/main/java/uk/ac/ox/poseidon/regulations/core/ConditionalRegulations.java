package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Mode;
import uk.ac.ox.poseidon.regulations.api.Regulations;

public class ConditionalRegulations implements Regulations {

    private final Condition condition;
    private final Regulations regulationsIfTrue;
    private final Regulations regulationsIfFalse;

    public ConditionalRegulations(
        final Condition condition,
        final Regulations regulationsIfTrue,
        final Regulations regulationsIfFalse
    ) {
        this.condition = condition;
        this.regulationsIfTrue = regulationsIfTrue;
        this.regulationsIfFalse = regulationsIfFalse;
    }

    public Condition getCondition() {
        return condition;
    }

    public Regulations getRegulationIfTrue() {
        return regulationsIfTrue;
    }

    public Regulations getRegulationIfFalse() {
        return regulationsIfFalse;
    }

    @Override
    public Mode mode(final Action action) {
        final Regulations regulations =
            condition.test(action) ?
                regulationsIfTrue :
                regulationsIfFalse;
        return regulations.mode(action);
    }

}
