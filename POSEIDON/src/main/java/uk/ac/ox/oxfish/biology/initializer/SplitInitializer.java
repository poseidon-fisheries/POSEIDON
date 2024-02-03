/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * A facade for TwoSpeciesBoxInitializer where species 0 lives on the top and species1 at the bottom of the map
 * Created by carrknight on 9/22/15.
 */
public class SplitInitializer extends TwoSpeciesBoxInitializer {


    public SplitInitializer(
        DoubleParameter carryingCapacity,
        double percentageLimitOnDailyMovement,
        double differentialPercentageToMove,
        LogisticGrowerInitializer grower
    ) {
        //box top Y will have to be reset when generateLocal is called as the map doesn't exist just yet
        super(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, false,
            carryingCapacity,
            new FixedDoubleParameter(1d),
            percentageLimitOnDailyMovement,
            differentialPercentageToMove,
            grower
        );

    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
        GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
        int mapWidthInCells, NauticalMap map
    ) {

        setLowestY(mapHeightInCells / 2);
        return super.generateLocal(biology, seaTile, random, mapHeightInCells, mapWidthInCells, map);

    }


}
