package uk.ac.ox.oxfish.regulation.conditions;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class AnyOf implements AlgorithmFactory<Condition> {
    private Collection<AlgorithmFactory<Condition>> conditions;

    public AnyOf() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public AnyOf(final AlgorithmFactory<Condition>... conditions) {
        this(ImmutableList.copyOf(conditions));
    }

    public AnyOf(final Collection<? extends AlgorithmFactory<Condition>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    public Collection<AlgorithmFactory<Condition>> getConditions() {
        return conditions;
    }

    public void setConditions(
        final Collection<? extends AlgorithmFactory<Condition>> conditions
    ) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @Override
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.AnyOf(
            conditions.stream()
                .map(condition -> condition.apply(fishState))
                .collect(toImmutableSet())
        );
    }
}