package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.NobodyFishesHereExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 6/7/16.
 */
public class NobodyFishesHereFactory implements AlgorithmFactory<NobodyFishesHereExtractor>
{

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NobodyFishesHereExtractor apply(FishState state) {
        return new NobodyFishesHereExtractor(); //easy
    }
}
