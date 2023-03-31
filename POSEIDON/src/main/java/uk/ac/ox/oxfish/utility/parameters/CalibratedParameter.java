package uk.ac.ox.oxfish.utility.parameters;

import uk.ac.ox.oxfish.parameters.FreeParameter;

public class CalibratedParameter extends FixedDoubleParameter {
    public CalibratedParameter() {
    }

    public CalibratedParameter(final double fixedValue) {
        super(fixedValue);
    }

    @Override
    @FreeParameter
    public void setFixedValue(final double fixedValue) {
        super.setFixedValue(fixedValue);
    }
}
