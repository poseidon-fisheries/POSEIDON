package uk.ac.ox.oxfish.geography.habitat;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Puts sandy tiles everywhere
 * Created by carrknight on 9/28/15.
 */
public class AllSandyHabitatInitializer implements HabitatInitializer {
    /**
     * Puts sandy tiles everywhere
     *
     * @param map the input argument
     * @param model
     */
    @Override
    public void applyHabitats(NauticalMap map, MersenneTwisterFast random, FishState model) {
        for(SeaTile tile : map.getAllSeaTilesAsList())
            tile.setHabitat(new TileHabitat(0d));
    }
}
