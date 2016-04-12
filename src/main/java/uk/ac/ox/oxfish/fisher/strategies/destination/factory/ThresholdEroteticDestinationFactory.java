package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.erotetic.FeatureFilter;
import uk.ac.ox.oxfish.fisher.erotetic.ThresholdFilter;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.ThresholdEroteticDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 4/11/16.
 */
public class ThresholdEroteticDestinationFactory implements AlgorithmFactory<ThresholdEroteticDestinationStrategy> {


    private DoubleParameter minimumObservations = new FixedDoubleParameter(5);

    private DoubleParameter profitThreshold = new FixedDoubleParameter(0);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ThresholdEroteticDestinationStrategy apply(FishState state) {
        return new ThresholdEroteticDestinationStrategy(
                new ThresholdFilter<>(
                        minimumObservations.apply(state.getRandom()).intValue(),
                        minimumObservations.apply(state.getRandom()),
                        FeatureFilter.LAST_PROFIT_FEATURE
                ),
                new FavoriteDestinationStrategy(state.getMap(),state.getRandom())

        );
    }

    public DoubleParameter getMinimumObservations() {
        return minimumObservations;
    }

    public void setMinimumObservations(DoubleParameter minimumObservations) {
        this.minimumObservations = minimumObservations;
    }

    public DoubleParameter getProfitThreshold() {
        return profitThreshold;
    }

    public void setProfitThreshold(DoubleParameter profitThreshold) {
        this.profitThreshold = profitThreshold;
    }


}
