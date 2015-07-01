package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishOnceStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class FishOnceFactory implements AlgorithmFactory<FishOnceStrategy>
{

    @Override
    public FishOnceStrategy apply(FishState state) {
        return new FishOnceStrategy();
    }
}
