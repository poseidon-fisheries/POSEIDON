package uk.ac.ox.oxfish.utility.parameters;

public class IntegerParameter extends FixedParameter<Integer> {
    public IntegerParameter(final Integer value) {
        super(value);
    }

    public int getIntValue() {
        return getValue();
    }
}
