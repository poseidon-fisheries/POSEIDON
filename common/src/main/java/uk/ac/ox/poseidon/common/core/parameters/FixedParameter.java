package uk.ac.ox.poseidon.common.core.parameters;

import uk.ac.ox.poseidon.common.api.parameters.Parameter;

import java.util.Objects;

public class FixedParameter<T> implements Parameter {
    private T value;

    protected FixedParameter(final T value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FixedParameter)) return false;
        final FixedParameter<?> that = (FixedParameter<?>) o;
        return Objects.equals(getValue(), that.getValue());
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
