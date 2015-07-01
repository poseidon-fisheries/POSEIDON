package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.maximization.HillClimbingMovement;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * creates a per-trip iterative destination strategy
 * Created by carrknight on 6/19/15.
 */
public class PerTripIterativeDestinationFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy>
{

    DoubleParameter tripsPerDecision = new FixedDoubleParameter(1d);

    DoubleParameter stepSize = new FixedDoubleParameter(5d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PerTripIterativeDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();


        final HillClimbingMovement algorithm = new HillClimbingMovement(map, random);
        algorithm.setMaxStepSize(stepSize.apply(state.random).intValue());
        final PerTripIterativeDestinationStrategy toReturn = new PerTripIterativeDestinationStrategy(
                new FavoriteDestinationStrategy(map, random), algorithm);
        toReturn.setTripsPerDecision(tripsPerDecision.apply(state.random).intValue());
        return toReturn;

    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }

    public DoubleParameter getTripsPerDecision() {
        return tripsPerDecision;
    }

    public void setTripsPerDecision(DoubleParameter tripsPerDecision) {
        this.tripsPerDecision = tripsPerDecision;
    }
}
