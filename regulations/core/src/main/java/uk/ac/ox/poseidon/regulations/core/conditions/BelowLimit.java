package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class BelowLimit implements Predicate<Action> {

    private final int limit;
    private final ToIntFunction<? super Action> counter;

    public BelowLimit(
        final int limit,
        final ToIntFunction<? super Action> counter
    ) {
        this.limit = limit;
        this.counter = counter;
    }

    @Override
    public boolean test(final Action action) {
        return counter.applyAsInt(action) < limit;
    }
}
