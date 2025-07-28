package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

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
            new CalibratedParameter(2, 6, 1, 8, 2.25) ,// margin
            new CalibratedParameter(25, 27, 24, 30, 26.5), // target2 [Skipjack]
            new CalibratedParameter(25, 27, 24, 30, 26.5) //target3 [Yellowfin]
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
