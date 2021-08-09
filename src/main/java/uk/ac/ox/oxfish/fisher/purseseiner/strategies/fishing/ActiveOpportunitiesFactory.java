package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class ActiveOpportunitiesFactory implements AlgorithmFactory<ActiveOpportunities> {
    @Override
    public ActiveOpportunities apply(FishState fishState) {
        ActiveOpportunities activeOpportunities = new ActiveOpportunities();
        fishState.registerStartable(activeOpportunities);
        return activeOpportunities;
    }
}
