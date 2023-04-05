package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class FrontalIndexMapFactory extends EnvironmentalMapFactory {
    public FrontalIndexMapFactory() {
    }

    public FrontalIndexMapFactory(
        final InputPath gridFile
    ) {
        super(
            "FrontalIndex",
            gridFile,
            new CalibratedParameter(0, 4, 0, 25, 0),
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
