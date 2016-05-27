package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.erotetic.AdaptiveThresholdFilter;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SNALSARutilities;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.ThresholdEroteticDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.function.Function;

/**
 * The Threshold Erotetic Destination Strategy where the threshold is the average
 * Created by carrknight on 4/11/16.
 */
public class BetterThanAverageEroteticDestinationFactory implements AlgorithmFactory<ThresholdEroteticDestinationStrategy>
{



    private DoubleParameter minimumObservations = new FixedDoubleParameter(5);


    private DoubleParameter updateInterval = new UniformDoubleParameter(5,15);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ThresholdEroteticDestinationStrategy apply(FishState state)
    {
        return new ThresholdEroteticDestinationStrategy(
                new AdaptiveThresholdFilter<>(
                        minimumObservations.apply(state.getRandom()).intValue(),
                        0d,
                        SNALSARutilities.PROFIT_FEATURE,
                        new Function<FishState, Double>() {
                            @Override
                            public Double apply(FishState simState) {
                                return simState.getFishers().stream().mapToDouble(
                                        value -> {
                                            TripRecord lastTrip = value.getLastFinishedTrip();
                                            if(lastTrip == null || !Double.isFinite(lastTrip.getProfitPerHour(true) ))
                                                return 0d;
                                            else
                                                return lastTrip.getProfitPerHour(true);
                                        }
                                ).average().getAsDouble();
                            }
                        },
                        updateInterval.apply(state.getRandom()).intValue()

                ),
                new FavoriteDestinationStrategy(state.getMap(),state.getRandom())

        );
    }


    /**
     * Getter for property 'minimumObservations'.
     *
     * @return Value for property 'minimumObservations'.
     */
    public DoubleParameter getMinimumObservations() {
        return minimumObservations;
    }

    /**
     * Setter for property 'minimumObservations'.
     *
     * @param minimumObservations Value to set for property 'minimumObservations'.
     */
    public void setMinimumObservations(DoubleParameter minimumObservations) {
        this.minimumObservations = minimumObservations;
    }

    public DoubleParameter getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(DoubleParameter updateInterval) {
        this.updateInterval = updateInterval;
    }
}
