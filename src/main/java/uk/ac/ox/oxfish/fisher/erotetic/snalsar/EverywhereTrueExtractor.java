package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FixedMap;

import java.util.Collection;
import java.util.Map;

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
    public Map<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent,
            FishState model, Fisher fisher)
    {



        return new FixedMap<>(1.0, toRepresent);

    }
}
