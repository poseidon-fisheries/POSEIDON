package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Creates the local biology object for each sea-tile and then is passed the full map to post-process (smooth/connect) the
 * various biologies
 * Created by carrknight on 6/22/15.
 */
public interface BiologyInitializer
{





    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     * @param biology the global biology (species' list) object
     * @param seaTile the sea-tile to populate
     * @param random the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells width of the map
     */
    LocalBiology generate(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells);


    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     * @param biology the global biology instance
     * @param map the map which by now should have all the tiles in place
     * @param model the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     *              than using getters
     */
    void processMap(GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model);


    int getNumberOfSpecies();

}
