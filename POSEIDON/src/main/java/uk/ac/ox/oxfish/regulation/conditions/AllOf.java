package uk.ac.ox.oxfish.regulation.conditions;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class AllOf implements AlgorithmFactory<Condition> {
    private Collection<AlgorithmFactory<Condition>> conditions;

    @SuppressWarnings("unused")
    public AllOf() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public AllOf(final AlgorithmFactory<Condition>... conditions) {
        this(ImmutableList.copyOf(conditions));
    }

    @SuppressWarnings("WeakerAccess")
    public AllOf(final Collection<? extends AlgorithmFactory<Condition>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @SuppressWarnings("unused")
    public AllOf(final Stream<? extends AlgorithmFactory<Condition>> conditions) {
        this(conditions.collect(toImmutableList()));
    }

    public Collection<AlgorithmFactory<Condition>> getConditions() {
        return conditions;
    }

    public void setConditions(final Collection<? extends AlgorithmFactory<Condition>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @Override
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.AllOf(
            conditions.stream()
                .map(condition -> condition.apply(fishState))
                .collect(toImmutableSet())
        );
    }
}
