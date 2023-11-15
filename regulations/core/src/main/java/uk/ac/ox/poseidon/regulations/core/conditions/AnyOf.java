package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Set;

public class AnyOf extends VariadicConditionalOperator {


    public AnyOf(Set<Condition> operands) {
        super(operands);
    }

    @Override
    public boolean test(final Action action) {
        return getSubConditions().stream().anyMatch(predicate -> predicate.test(action));
    }

    @Override
    public String toString() {
        return "AnyOf{" +
            "conditions=" + getSubConditions() +
            '}';
    }
}