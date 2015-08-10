package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.YearlyIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * factory that creates an hill-climber strategy with a random starting point
 */
public class YearlyIterativeDestinationFactory implements AlgorithmFactory<YearlyIterativeDestinationStrategy>
{

    DoubleParameter stepSize = new FixedDoubleParameter(5d);



    @Override
    public YearlyIterativeDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();


        return new YearlyIterativeDestinationStrategy(new FavoriteDestinationStrategy(map,random),
                                                      stepSize.apply(random).intValue(),
                                                      10);

    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }
}