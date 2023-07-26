package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class TemperatureMapFactory extends EnvironmentalMapFactory {
    public TemperatureMapFactory() {
    }

    public TemperatureMapFactory(
        final InputPath gridFile
    ) {
        super(
            "Temperature",
            gridFile,
                new CalibratedParameter(26, 27, 25, 28, 26.5), //target
                new CalibratedParameter(1, 3, 0, 5, 2), //penalty
                new CalibratedParameter(1.0, 3.0, 0.5, 5.0, 2.25) //margin
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
    public void setMargin(final DoubleParameter margin) {super.setMargin(margin);}
}
