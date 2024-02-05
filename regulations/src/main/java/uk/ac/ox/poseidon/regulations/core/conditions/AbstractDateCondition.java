package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class AbstractDateCondition implements Condition {
    @Override
    public boolean test(final Action action) {
        return action
            .getDateTime()
            .map(LocalDateTime::toLocalDate)
            .map(this::test)
            .orElse(false);
    }

    abstract boolean test(LocalDate date);
}
