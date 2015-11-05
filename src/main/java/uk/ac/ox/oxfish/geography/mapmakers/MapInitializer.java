package uk.ac.ox.oxfish.geography.mapmakers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Object used to create the map for a simulation
 * Created by carrknight on 11/5/15.
 */
public interface MapInitializer {


    public NauticalMap makeMap(MersenneTwisterFast random, GlobalBiology biology, FishState model);


}
