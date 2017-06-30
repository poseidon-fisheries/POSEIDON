package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A facade for TwoSpeciesBoxInitializer where species 0 lives on the top and species1 at the bottom of the map
 * Created by carrknight on 9/22/15.
 */
public class SplitInitializer extends TwoSpeciesBoxInitializer {


    public SplitInitializer(DoubleParameter carryingCapacity,
                            double percentageLimitOnDailyMovement,
                            double differentialPercentageToMove,
                            LogisticGrowerInitializer grower
    ) {
        //box top Y will have to be reset when generateLocal is called as the map doesn't exist just yet
        super(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE,false,
              carryingCapacity,
              new FixedDoubleParameter(1d),
              percentageLimitOnDailyMovement,
              differentialPercentageToMove,
              grower);

    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *  @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells, NauticalMap map) {

        setLowestY(mapHeightInCells / 2);
        return super.generateLocal(biology, seaTile, random, mapHeightInCells, mapWidthInCells, map);

    }


}
