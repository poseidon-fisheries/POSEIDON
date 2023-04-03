package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.parameters.FreeParameter;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class ChlorophyllMapFactory extends EnvironmentalMapFactory {
    public ChlorophyllMapFactory() {
    }

    public ChlorophyllMapFactory(
        final InputPath gridFile
    ) {
        super(
            "Chlorophyll",
            gridFile,
            new CalibratedParameter(),
            new CalibratedParameter(2.0)
        );
    }

    @Override
    @FreeParameter
    public void setPenalty(final DoubleParameter penalty) {
        super.setPenalty(penalty);
    }

    @Override
    @FreeParameter
    public void setThreshold(final DoubleParameter threshold) {
        super.setThreshold(threshold);
    }
}
