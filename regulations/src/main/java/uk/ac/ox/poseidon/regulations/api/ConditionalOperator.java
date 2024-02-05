package uk.ac.ox.poseidon.regulations.api;

import java.util.Set;

public interface ConditionalOperator extends Condition {
    Set<Condition> getSubConditions();
}
