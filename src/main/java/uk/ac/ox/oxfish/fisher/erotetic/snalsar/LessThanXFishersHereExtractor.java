package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;

/**
 * If there are fewer than x fishers in this location then it returns 1.0 otherwise -1.0
 * Created by carrknight on 5/26/16.
 */
public class LessThanXFishersHereExtractor implements
        SafetyFeatureExtractor<SeaTile>, SocialAcceptabilityFeatureExtractor<SeaTile>
{


    private int minimumNumberOfFishersBeforeFalse = 1;

    public LessThanXFishersHereExtractor() {
    }

    public LessThanXFishersHereExtractor(int minimumNumberOfFishersBeforeFalse) {
        this.minimumNumberOfFishersBeforeFalse = minimumNumberOfFishersBeforeFalse;
    }

    /**
     * Getter for property 'minimumNumberOfFishersBeforeFalse'.
     *
     * @return Value for property 'minimumNumberOfFishersBeforeFalse'.
     */
    public int getMinimumNumberOfFishersBeforeFalse() {
        return minimumNumberOfFishersBeforeFalse;
    }

    /**
     * Setter for property 'minimumNumberOfFishersBeforeFalse'.
     *
     * @param minimumNumberOfFishersBeforeFalse Value to set for property 'minimumNumberOfFishersBeforeFalse'.
     */
    public void setMinimumNumberOfFishersBeforeFalse(int minimumNumberOfFishersBeforeFalse) {
        this.minimumNumberOfFishersBeforeFalse = minimumNumberOfFishersBeforeFalse;
    }

    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     *  @param toRepresent the list of object from which to extract a feature
     * @param model       the model to represent
     * @param fisher
     */
    @Override
    public HashMap<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent, FishState model, Fisher fisher) {

        HashMap<SeaTile,Double> toReturn = new HashMap<>(toRepresent.size());
        for(SeaTile tile : toRepresent)
        {
            if(model.getFishersAtLocation(tile).size()>= minimumNumberOfFishersBeforeFalse)
                toReturn.put(tile, -1d); //unsafe
            else
                toReturn.put(tile, 1d); //safe

        }
        return toReturn;
    }
}
