package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.util.ArrayList;
import java.util.stream.Stream;

public class MixingLayerMapFactory extends EnvironmentalMapFactory {
    public MixingLayerMapFactory() {
    }

    public MixingLayerMapFactory(
        final InputPath gridFile,
        final int mapPeriod
    ) {
        super(
            "MixingLayerDepth",
            gridFile,
            new IntegerParameter(mapPeriod),
            new CalibratedParameter(30, 50, 0, 75, 26.5), // target
            new CalibratedParameter(.25, .75, 0, 1), // penalty
            new CalibratedParameter(5, 15, 1, 20, 10) // margin
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
