package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;

/**
 * Returns 1.0 to everything
 * Created by carrknight on 5/26/16.
 */
public class EverywhereTrueExtractor implements SafetyFeatureExtractor<SeaTile>, LegalityFeatureExtractor<SeaTile>,
        SocialAcceptabilityFeatureExtractor<SeaTile>
{

    /**
     * Returns a map where everything is assigned value 1 (safe)
     */
    @Override
    public HashMap<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent,
            FishState model, Fisher fisher)
    {

        HashMap<SeaTile,Double> toReturn = new HashMap<>(toRepresent.size());
        for(SeaTile tile : toRepresent)
            toReturn.put(tile,1.0);

        return toReturn;

    }
}
