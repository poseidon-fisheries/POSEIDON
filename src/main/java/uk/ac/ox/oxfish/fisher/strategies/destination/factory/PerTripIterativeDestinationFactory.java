package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.maximization.DefaultBeamHillClimbing;
import uk.ac.ox.oxfish.utility.maximization.HillClimbingMovement;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * creates a per-trip iterative destination strategy
 * Created by carrknight on 6/19/15.
 */
public class PerTripIterativeDestinationFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy>
{


    DoubleParameter stepSize = new FixedDoubleParameter(5d);

    DoubleParameter stayingStillProbability = new FixedDoubleParameter(0d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PerTripIterativeDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.getRandom();
        NauticalMap map = state.getMap();


        final DefaultBeamHillClimbing algorithm = new DefaultBeamHillClimbing(stepSize.apply(random).intValue(),
                                                                           20);
        return new PerTripIterativeDestinationStrategy(
                new FavoriteDestinationStrategy(map, random), algorithm,1d-stayingStillProbability.apply(random),0d);

    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }


    public DoubleParameter getStayingStillProbability() {
        return stayingStillProbability;
    }

    public void setStayingStillProbability(DoubleParameter stayingStillProbability) {
        this.stayingStillProbability = stayingStillProbability;
    }
}
