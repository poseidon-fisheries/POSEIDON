package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.FollowRulesExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;


public class FollowRulesExtractorFactory implements AlgorithmFactory<FollowRulesExtractor> {

    @Override
    public FollowRulesExtractor apply(FishState state)
    {
        return new FollowRulesExtractor();
    }
}