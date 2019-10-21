package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FollowPlanFadFishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class FollowPlanFadFishingStrategyFactory implements
    AlgorithmFactory<FollowPlanFadFishingStrategy> {

    @Override public FollowPlanFadFishingStrategy apply(FishState fishState) {
        return new FollowPlanFadFishingStrategy();
    }
}
