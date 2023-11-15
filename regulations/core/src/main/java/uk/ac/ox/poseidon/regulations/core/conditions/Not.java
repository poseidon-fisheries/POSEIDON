package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class Not extends MonadicConditionalOperator {

    public Not(Condition operand) {
        super(operand);
    }

    @Override
    public boolean test(final Action action) {
        return !getSubCondition().test(action);
    }

    @Override
    public String toString() {
        return "Not{" +
            "condition=" + getSubCondition() +
            '}';
    }
}
