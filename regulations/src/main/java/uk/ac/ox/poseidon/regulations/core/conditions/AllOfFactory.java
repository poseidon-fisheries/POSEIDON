package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class AllOfFactory implements ComponentFactory<Condition> {
    private Collection<ComponentFactory<Condition>> conditions;

    @SuppressWarnings("unused")
    public AllOfFactory() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public AllOfFactory(final ComponentFactory<Condition>... conditions) {
        this(ImmutableList.copyOf(conditions));
    }

    @SuppressWarnings("WeakerAccess")
    public AllOfFactory(final Collection<? extends ComponentFactory<Condition>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @SuppressWarnings("unused")
    public AllOfFactory(final Stream<? extends ComponentFactory<Condition>> conditions) {
        this(conditions.collect(toImmutableList()));
    }

    public Collection<ComponentFactory<Condition>> getConditions() {
        return conditions;
    }

    public void setConditions(final Collection<? extends ComponentFactory<Condition>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @Override
    public Condition apply(final ModelState modelState) {
        return new AllOf(
            conditions.stream()
                .map(condition -> condition.apply(modelState))
                .collect(toImmutableSet())
        );
    }
}
