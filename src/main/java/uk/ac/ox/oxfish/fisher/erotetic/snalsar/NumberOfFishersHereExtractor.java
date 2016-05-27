package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;

/**
 * Returns the number of fishers at any location
 * Created by carrknight on 5/26/16.
 */
public class NumberOfFishersHereExtractor implements SafetyFeatureExtractor<SeaTile>,
        SocialAcceptabilityFeatureExtractor<SeaTile>
{


    public NumberOfFishersHereExtractor() {
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
            Bag fishersAtLocation = model.getFishersAtLocation(tile);
            double value = fishersAtLocation == null ? 0 : fishersAtLocation.size();
            toReturn.put(tile, value);

        }
        return toReturn;
    }
}
