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
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A diffusing logistic initializer where the bycatch specie only exists on the upper half of the sea.
 * Created by carrknight on 7/30/15.
 */
public class HalfBycatchInitializer extends TwoSpeciesBoxInitializer {





    public HalfBycatchInitializer(DoubleParameter carryingCapacity,
                                  double percentageLimitOnDailyMovement,
                                  double differentialPercentageToMove,
                                  LogisticGrowerInitializer grower) {
        super(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE,true,
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

    /**
     * creates data columns
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {

        super.processMap(biology, map, random, model);

        int width = map.getHeight();
        int heightLow = map.getHeight() / 2;
        int heightHigh = heightLow+1;


        //create daily data
        //record info:
        DataColumn dailyNorthColumn = model.getDailyDataSet().registerGatherer("# of North Tows", state -> {
            double towsNorth = 0;
            for (int x = 0; x < width; x++)
                for (int y = 0; y < heightLow; y++)
                    towsNorth += map.getDailyTrawlsMap().get(x, y);

            return towsNorth;
        }, Double.NaN);
        DataColumn dailySouthColumn = model.getDailyDataSet().registerGatherer("# of South Tows", state -> {
            double towsSouth = 0;
            for (int x = 0; x < width; x++)
                for (int y = heightHigh; y < map.getHeight(); y++)
                    towsSouth += map.getDailyTrawlsMap().get(x, y);

            return towsSouth;
        }, Double.NaN);


        //create yearly data
        model.getYearlyDataSet().registerGatherer("# of North Tows",
                                                  FishStateUtilities.generateYearlySum(dailyNorthColumn),
                                                  Double.NaN);
        model.getYearlyDataSet().registerGatherer("# of South Tows",
                                                  FishStateUtilities.generateYearlySum(dailySouthColumn),
                                                  Double.NaN);
    }


}
