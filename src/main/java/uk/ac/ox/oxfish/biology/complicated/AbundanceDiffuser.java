package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

/**
 * Marker for all diffuser objects that deal with abundance
 * Created by carrknight on 7/6/17.
 */
public interface AbundanceDiffuser {



    public void step(Species species,
                     Map<SeaTile, AbundanceBasedLocalBiology> biologies,
                     FishState state);


}
