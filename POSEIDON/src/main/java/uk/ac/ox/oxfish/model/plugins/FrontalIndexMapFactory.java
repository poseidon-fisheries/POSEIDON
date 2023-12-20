package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

public class FrontalIndexMapFactory extends EnvironmentalMapFactory {
    public FrontalIndexMapFactory() {
    }

    public FrontalIndexMapFactory(
        final InputPath gridFile,
        final int mapPeriod
    ) {
        super(
            "FrontalIndex",
            gridFile,
            new IntegerParameter(mapPeriod),
            new CalibratedParameter(0, 2, 0, 3), // target
            new CalibratedParameter(.25, .75, 0, 1), // penalty
            new CalibratedParameter(0, 2, 0, 2) // margin
        );
    }

    @Override
    public void setPenalty(final DoubleParameter penalty) {
        super.setPenalty(penalty);
    }

    @Override
    public void setTarget(final DoubleParameter target) {
        super.setTarget(target);
    }

    @Override
    public void setMargin(final DoubleParameter margin) {
        super.setMargin(margin);
    }
}
