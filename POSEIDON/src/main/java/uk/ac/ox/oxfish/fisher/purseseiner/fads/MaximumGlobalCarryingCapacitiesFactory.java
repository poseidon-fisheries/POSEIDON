package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class MaximumGlobalCarryingCapacitiesFactory extends GlobalCarryingCapacitiesFactory {
    @Override
    DoubleParameter makeGlobalCarryingCapacityParameter() {
        return new FixedDoubleParameter(Double.MAX_VALUE);
    }
}
