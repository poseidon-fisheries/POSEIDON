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
            new CalibratedParameter(-2.0, 0, 0, 0, -0.20), //target
            new CalibratedParameter(1, 3, 0, 5, 2), //penalty
            new CalibratedParameter(2.0, 3.0, 0, 25.0, 2) //margin
        );
    }

    @Override
    public void setPenalty(final DoubleParameter penalty) {super.setPenalty(penalty);}

    @Override
    public void setTarget(final DoubleParameter target) {
        super.setTarget(target);
    }

    @Override
    public void setMargin(final DoubleParameter margin) {
        super.setMargin(margin);
    }
}
