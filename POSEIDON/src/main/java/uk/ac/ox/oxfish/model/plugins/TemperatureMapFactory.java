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
            new CalibratedParameter(10, 35, 10, 40, 28),
            new CalibratedParameter(1, 4, 1, 4, 2)
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
