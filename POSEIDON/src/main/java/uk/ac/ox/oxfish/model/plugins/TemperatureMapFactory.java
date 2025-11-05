package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

/**
 * Modified by Brian Powers 7/2025 for the Eastern Atlantic Tuna Model
 *
 * The big change added is that this class can take in a different margin & target for each species. Many more parameters for added flexibility.
 * There is a better way to implement this, for future work to clean up the code - but it gets the job done.
 **/


public class TemperatureMapFactory extends EnvironmentalMapFactory {
    public TemperatureMapFactory() {
    }

    public TemperatureMapFactory(
        final InputPath gridFile,
        final int mapPeriod
    ) {
        super(
            "Temperature",
            gridFile,
            new IntegerParameter(mapPeriod),
            new CalibratedParameter(25, 27, 24, 30, 26.5),// target [Bigeye]
            new CalibratedParameter(.25, .75, 0, 1), // penalty
            new CalibratedParameter(2, 6, 2, 8, 2.25) ,// margin [Bigeye]
            new CalibratedParameter(25, 27, 24, 30, 26.5), // target2 [Skipjack]
            new CalibratedParameter(25, 27, 24, 30, 26.5), //target3 [Yellowfin]
            new CalibratedParameter(2,6,2,8,2.25), //margin2 [Skipjack]
            new CalibratedParameter(2,6,2,8,2.25) // margin3 [Yellowfin]
        );
    }

    @Override
    public void setPenalty(final DoubleParameter penalty) {
        super.setPenalty(penalty);
    }

    @Override
    public void setTarget(final DoubleParameter[] target) {
        super.setTarget(target);
    }

    public void setTarget(final DoubleParameter target){
        super.setTarget(target);
    }
    public void setTarget2(final DoubleParameter target2){
        super.setTarget2(target2);
    }
    public void setTarget3(final DoubleParameter target3){
        super.setTarget3(target3);
    }


    @Override
    public void setMargin(final DoubleParameter margin) {
        super.setMargin(margin);
    }
}
