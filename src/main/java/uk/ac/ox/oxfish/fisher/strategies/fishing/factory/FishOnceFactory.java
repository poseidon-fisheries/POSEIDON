package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishOnceStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

public class FishOnceFactory implements StrategyFactory<FishOnceStrategy>
{

    @Override
    public FishOnceStrategy apply(FishState state) {
        return new FishOnceStrategy();
    }
}
