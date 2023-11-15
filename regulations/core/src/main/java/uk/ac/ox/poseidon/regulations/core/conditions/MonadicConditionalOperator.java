package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.ConditionalOperator;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class MonadicConditionalOperator implements ConditionalOperator {

    @Override
    public Set<Condition> getSubConditions() {
        return ImmutableSet.of(condition);
    }

    public Condition getSubCondition() {
        return condition;
    }

    private final Condition condition;

    MonadicConditionalOperator(Condition condition) {
        this.condition = checkNotNull(condition);
    }
}
