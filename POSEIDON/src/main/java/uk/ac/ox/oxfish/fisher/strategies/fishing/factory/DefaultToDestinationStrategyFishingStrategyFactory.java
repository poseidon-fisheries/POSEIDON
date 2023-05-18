package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.DefaultToDestinationStrategyFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class DefaultToDestinationStrategyFishingStrategyFactory implements
    AlgorithmFactory<DefaultToDestinationStrategyFishingStrategy> {

    @Override
    public DefaultToDestinationStrategyFishingStrategy apply(FishState state) {
        return new DefaultToDestinationStrategyFishingStrategy();
    }
}
