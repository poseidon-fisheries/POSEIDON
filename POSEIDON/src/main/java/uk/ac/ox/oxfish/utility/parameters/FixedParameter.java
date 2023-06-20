package uk.ac.ox.oxfish.utility.parameters;

public class FixedParameter<T> implements Parameter {
    private T value;

    FixedParameter(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }
}
