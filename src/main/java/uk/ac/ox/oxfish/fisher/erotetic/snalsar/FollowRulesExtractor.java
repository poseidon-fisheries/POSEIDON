package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Properly follow all the rules (am I allowed at sea? am I allowed THERE?)
 * Created by carrknight on 5/26/16.
 */
public class FollowRulesExtractor implements LegalityFeatureExtractor<SeaTile>
{

    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     *  @param toRepresent the list of object from which to extract a feature
     * @param model       the model to represent
     * @param fisher
     */
    @Override
    public Map<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent, FishState model, Fisher fisher) {
        HashMap<SeaTile,Double> toReturn = new HashMap<>();
        for(SeaTile tile : toRepresent)
        {
            boolean allowed = fisher.isAllowedAtSea() && fisher.isAllowedToFishHere(tile,model);
            toReturn.put(tile,allowed ? 1.0 : -1.0);
        }
        return toReturn;
    }
}
