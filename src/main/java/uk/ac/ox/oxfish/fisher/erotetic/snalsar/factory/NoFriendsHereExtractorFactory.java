package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.NoFriendsHereExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 5/31/16.
 */
public class NoFriendsHereExtractorFactory implements AlgorithmFactory<NoFriendsHereExtractor>
{


    /**
     * when false the fisher will ALSO consider the locations of people who call this fisher a friend (even if the feeling
     * isn't mutual)
     */
    private boolean onlyDirectedLinks = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NoFriendsHereExtractor apply(FishState state) {
        return new NoFriendsHereExtractor(onlyDirectedLinks);
    }

    /**
     * Getter for property 'onlyDirectedLinks'.
     *
     * @return Value for property 'onlyDirectedLinks'.
     */
    public boolean isOnlyDirectedLinks() {
        return onlyDirectedLinks;
    }

    /**
     * Setter for property 'onlyDirectedLinks'.
     *
     * @param onlyDirectedLinks Value to set for property 'onlyDirectedLinks'.
     */
    public void setOnlyDirectedLinks(boolean onlyDirectedLinks) {
        this.onlyDirectedLinks = onlyDirectedLinks;
    }
}
