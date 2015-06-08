package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.RandomThenBackToPortDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

public class RandomThenBackToPortFactory implements StrategyFactory<RandomThenBackToPortDestinationStrategy>
{

    @Override
    public RandomThenBackToPortDestinationStrategy apply(FishState state) {
        return new RandomThenBackToPortDestinationStrategy();
    }
}
