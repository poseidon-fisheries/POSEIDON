package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class AboveLimit implements Predicate<Action> {

    private final int limit;
    private final ToIntFunction<? super Action> extractCount;

    public AboveLimit(
        final int limit,
        final ToIntFunction<? super Action> extractCount
    ) {
        this.limit = limit;
        this.extractCount = extractCount;
    }

    @Override
    public boolean test(final Action action) {
        return extractCount.applyAsInt(action) > limit;
    }
}
