package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.RandomFadFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class RandomFadFishingStrategyFactory implements AlgorithmFactory<RandomFadFishingStrategy> {

    @Override
    public RandomFadFishingStrategy apply(FishState model) {
        return new RandomFadFishingStrategy();
    }
}
