package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.parameters.FreeParameter;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class ChlorophyllThresholdParameter extends CalibratedParameter {
    @Override
    @FreeParameter(hardMaximum = 1)
    public void setFixedValue(final double fixedValue) {
        super.setFixedValue(fixedValue);
    }
}
