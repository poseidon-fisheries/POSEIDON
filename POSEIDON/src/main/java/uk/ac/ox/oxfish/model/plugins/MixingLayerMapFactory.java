package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.util.ArrayList;
import java.util.stream.Stream;

/*
Factory to produce a MixingLayerDepth environmental parameter
Created by Brian Powers 5/21/2025 for the Eastern Atlantic Tuna Model
This environmental map works like others. It can be used in other fisheries as an environmental layer.
 */

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
            new CalibratedParameter(25, 50, 0, 75, 26.5), // target
            new CalibratedParameter(.05, .25, 0, 1), // penalty
            new CalibratedParameter(25, 40, 1, 50, 10) // margin
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
