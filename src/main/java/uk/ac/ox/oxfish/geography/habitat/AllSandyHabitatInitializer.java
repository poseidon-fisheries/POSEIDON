package uk.ac.ox.oxfish.geography.habitat;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Puts sandy tiles everywhere
 * Created by carrknight on 9/28/15.
 */
public class AllSandyHabitatInitializer implements HabitatInitializer {
    /**
     * Puts sandy tiles everywhere
     *
     * @param nauticalMap the input argument
     */
    @Override
    public void accept(NauticalMap nauticalMap) {
        for(SeaTile tile : nauticalMap.getAllSeaTilesAsList())
            tile.setHabitat(new TileHabitat(0d));
    }
}
