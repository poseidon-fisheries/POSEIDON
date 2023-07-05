package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.Collection;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class AllOf implements AlgorithmFactory<Predicate<Action>> {
    private Collection<AlgorithmFactory<Predicate<Action>>> conditions;

    public AllOf() {
    }

    public AllOf(final Collection<AlgorithmFactory<Predicate<Action>>> conditions) {
        this.conditions = conditions;
    }

    public Collection<AlgorithmFactory<Predicate<Action>>> getConditions() {
        return conditions;
    }

    public void setConditions(final Collection<AlgorithmFactory<Predicate<Action>>> conditions) {
        this.conditions = conditions;
    }

    @Override
    public Predicate<Action> apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.AllOf(
            conditions.stream()
                .map(condition -> condition.apply(fishState))
                .collect(toImmutableSet())
        );
    }
}
