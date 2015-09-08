package uk.ac.ox.oxfish.fisher.strategies.weather.factory;

import uk.ac.ox.oxfish.fisher.strategies.weather.WindThresholdStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

/**
 * Creates WindThreshold strategies
 * Created by carrknight on 9/8/15.
 */
public class WindThresholdFactory  implements AlgorithmFactory<WindThresholdStrategy> {



    private DoubleParameter maximumWindSpeedTolerated = new NormalDoubleParameter(50,10);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public WindThresholdStrategy apply(FishState state) {
        return new WindThresholdStrategy(maximumWindSpeedTolerated.apply(state.getRandom()));
    }

    public DoubleParameter getMaximumWindSpeedTolerated() {
        return maximumWindSpeedTolerated;
    }

    public void setMaximumWindSpeedTolerated(DoubleParameter maximumWindSpeedTolerated) {
        this.maximumWindSpeedTolerated = maximumWindSpeedTolerated;
    }
}
