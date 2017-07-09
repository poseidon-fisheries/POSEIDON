package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

/**
 * Simply assumes no movement
 * Created by carrknight on 7/6/17.
 */
public class NoAbundanceDiffusion implements AbundanceDiffuser {


    @Override
    public void step(
            Species species,
            Map<SeaTile, AbundanceBasedLocalBiology> biologies,
            FishState state) {

    }
}
