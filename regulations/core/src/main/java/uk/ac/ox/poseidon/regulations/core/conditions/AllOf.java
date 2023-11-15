package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Set;

public class AllOf extends VariadicConditionalOperator {

    public AllOf(Set<Condition> operands) {
        super(operands);
    }

    @Override
    public boolean test(final Action action) {
        return getSubConditions().stream().allMatch(predicate -> predicate.test(action));
    }

    @Override
    public String toString() {
        return "AllOf{" +
            "conditions=" + getSubConditions() +
            '}';
    }
}
