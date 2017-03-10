package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

/**
 * An auxiliary method to create and schedule a logistic grower.
 * Created by carrknight on 1/31/17.
 */
public interface LogisticGrowerInitializer {


    public void initializeGrower(Map<SeaTile,BiomassLocalBiology> tiles,
                                 FishState state,
                                 MersenneTwisterFast random);
}
