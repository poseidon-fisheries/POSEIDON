package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.ThresholdExplorationProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Created by carrknight on 10/17/16.
 */
public class SocialAnnealingProbabilityFactory implements AlgorithmFactory<ThresholdExplorationProbability>{


    private DoubleParameter multiplier = new FixedDoubleParameter(1);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ThresholdExplorationProbability apply(FishState state) {
        return new ThresholdExplorationProbability(multiplier.apply(state.getRandom()),
                                                   new Function<FishState, Double>() {
                                                       @Override
                                                       public Double apply(FishState model) {
                                                           return model.getLatestDailyObservation(
                                                                   FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                                                       }
                                                   });
    }


    /**
     * Getter for property 'multiplier'.
     *
     * @return Value for property 'multiplier'.
     */
    public DoubleParameter getMultiplier() {
        return multiplier;
    }

    /**
     * Setter for property 'multiplier'.
     *
     * @param multiplier Value to set for property 'multiplier'.
     */
    public void setMultiplier(DoubleParameter multiplier) {
        this.multiplier = multiplier;
    }
}
