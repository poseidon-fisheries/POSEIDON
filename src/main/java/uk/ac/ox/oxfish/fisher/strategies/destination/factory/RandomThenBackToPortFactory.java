package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.RandomThenBackToPortDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

public class RandomThenBackToPortFactory implements StrategyFactory<RandomThenBackToPortDestinationStrategy>
{
    @Override
    public Class<? super RandomThenBackToPortDestinationStrategy> getStrategySuperClass() {
        return DestinationStrategy.class;
    }

    @Override
    public RandomThenBackToPortDestinationStrategy apply(FishState state) {
        return new RandomThenBackToPortDestinationStrategy();
    }
}
