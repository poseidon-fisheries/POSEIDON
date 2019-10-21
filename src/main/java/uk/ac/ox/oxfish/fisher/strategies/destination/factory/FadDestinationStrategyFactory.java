package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.FadDeploymentDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FadDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FadSettingDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class FadDestinationStrategyFactory implements AlgorithmFactory<FadDestinationStrategy> {

    @Override
    public FadDestinationStrategy apply(FishState fishState) {
        return new FadDestinationStrategy(
            new FadDeploymentDestinationStrategy(fishState.getMap()),
            new FadSettingDestinationStrategy(fishState.getMap())
        );
    }

}
