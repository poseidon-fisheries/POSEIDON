package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
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
            new CalibratedParameter(0, 0.15, 0, 0.5, 0.1),
            new CalibratedParameter(1, 3, 1, 3, 2)
        );
    }

    @Override
    public void setPenalty(final DoubleParameter penalty) {
        super.setPenalty(penalty);
    }

    @Override
    public void setThreshold(final DoubleParameter threshold) {
        super.setThreshold(threshold);
    }
}
