package uk.ac.ox.oxfish.utility.parameters;

import uk.ac.ox.poseidon.common.core.parameters.FixedParameter;

public class BooleanParameter extends FixedParameter<Boolean> {
    public BooleanParameter(final Boolean value) {
        super(value);
    }

    public boolean getBooleanValue() {
        return getValue();
    }
}
