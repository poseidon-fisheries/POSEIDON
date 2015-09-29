package uk.ac.ox.oxfish.geography.habitat;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;

/**
 * A consumer of a nautical map that sets TileHabitat objects for each sea tile
 * Created by carrknight on 9/28/15.
 */
public interface HabitatInitializer
{


    public void applyHabitats(NauticalMap map, MersenneTwisterFast random);


}
