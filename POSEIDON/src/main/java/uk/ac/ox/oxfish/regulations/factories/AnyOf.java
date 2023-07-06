package uk.ac.ox.oxfish.regulations.factories;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.Collection;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class AnyOf implements AlgorithmFactory<Predicate<Action>> {
    private Collection<AlgorithmFactory<Predicate<Action>>> conditions;

    public AnyOf() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public AnyOf(final AlgorithmFactory<Predicate<Action>>... conditions) {
        this(ImmutableList.copyOf(conditions));
    }

    public AnyOf(final Collection<? extends AlgorithmFactory<Predicate<Action>>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    public Collection<AlgorithmFactory<Predicate<Action>>> getConditions() {
        return conditions;
    }

    public void setConditions(
        final Collection<? extends AlgorithmFactory<Predicate<Action>>> conditions
    ) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @Override
    public Predicate<Action> apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.AnyOf(
            conditions.stream()
                .map(condition -> condition.apply(fishState))
                .collect(toImmutableSet())
        );
    }
}
