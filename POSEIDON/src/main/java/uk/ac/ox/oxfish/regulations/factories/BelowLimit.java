package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class BelowLimit implements AlgorithmFactory<Predicate<Action>> {

    private AlgorithmFactory<? extends ToIntFunction<? super Action>> counter;
    private IntegerParameter limit;

    public BelowLimit() {
    }

    public BelowLimit(
        final int limit,
        final AlgorithmFactory<? extends ToIntFunction<? super Action>> counter
    ) {
        this(new IntegerParameter(limit), counter);
    }

    public BelowLimit(
        final IntegerParameter limit,
        final AlgorithmFactory<? extends ToIntFunction<? super Action>> counter
    ) {
        this.limit = limit;
        this.counter = counter;
    }

    public AlgorithmFactory<? extends ToIntFunction<? super Action>> getCounter() {
        return counter;
    }

    public void setCounter(final AlgorithmFactory<? extends ToIntFunction<? super Action>> counter) {
        this.counter = counter;
    }

    public IntegerParameter getLimit() {
        return limit;
    }

    public void setLimit(final IntegerParameter limit) {
        this.limit = limit;
    }

    @Override
    public Predicate<Action> apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.BelowLimit(
            limit.getValue(),
            counter.apply(fishState)
        );
    }
}
