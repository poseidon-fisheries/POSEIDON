package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.ConditionalOperator;

import java.util.Set;

abstract class VariadicConditionalOperator implements ConditionalOperator {
    @Override
    public Set<Condition> getSubConditions() {
        return conditions;
    }

    private final Set<Condition> conditions;

    VariadicConditionalOperator(Set<Condition> conditions) {
        this.conditions = ImmutableSet.copyOf(conditions);
    }
}
