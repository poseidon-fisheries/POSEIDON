package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

public class RandomFavoriteDestinationFactory implements StrategyFactory<FavoriteDestinationStrategy>
{
    @Override
    public Class<? super FavoriteDestinationStrategy> getStrategySuperClass()
    {
        return  DestinationStrategy.class;
    }

    @Override
    public FavoriteDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();
        return new FavoriteDestinationStrategy(map,random);

    }
}
