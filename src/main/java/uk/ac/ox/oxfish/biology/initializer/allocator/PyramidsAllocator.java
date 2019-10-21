/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class PyramidsAllocator implements BiomassAllocator {

    private final int numberOfPeaks;

    private final double smoothingValue;

    private final int maxSpread;

    private final double peakBiomass;


    public PyramidsAllocator(int numberOfPeaks, double smoothingValue, int maxSpread, double peakBiomass) {
        this.numberOfPeaks = numberOfPeaks;
        this.smoothingValue = smoothingValue;
        this.maxSpread = maxSpread;
        this.peakBiomass = peakBiomass;
    }

    private DoubleGrid2D biomass;

    /**
     * Returns a positive number representing the weight in terms of either
     * biomass or carrying capacity (or whatever else the allocator is used for)
     *
     * @param tile   tile to allocate a weight to
     * @param map    general map information
     * @param random
     * @return
     */
    @Override
    public double allocate(
            SeaTile tile, NauticalMap map, MersenneTwisterFast random) {

        if(biomass==null) {
            int mapWidth = map.getWidth();
            int mapHeight = map.getHeight();
            biomass = new DoubleGrid2D(mapWidth,
                                       mapHeight,
                                       0d);

            for (int i = 0; i < numberOfPeaks; i++) {

                //create the bottom left corner
                int x;
                int y;
                do {
                    x = random.nextInt(mapWidth);
                    y = random.nextInt(mapHeight);
                }
                while (map.getSeaTile(x, y).isLand());
                biomass.set(x, y, peakBiomass);
                for (int spread = 1; spread < maxSpread; spread++) {


                    //vertical border
                    for (int h = -spread; h <= spread; h++) {
                        SeaTile border = map.getSeaTile(x - spread, y + h);
                        if (border != null && border.isWater()) {

                            biomass.set(x - spread, y + h,
                                        Math.min(biomass.get(x-spread,y+h) + Math.pow(smoothingValue, spread) * peakBiomass,peakBiomass));

                        }
                        border = map.getSeaTile(x + spread, y + h);
                        if (border != null && border.isWater()) {
                            biomass.set(x + spread, y + h,
                                        Math.min(biomass.get(x+spread,y+h) + Math.pow(smoothingValue, spread) * peakBiomass,peakBiomass));
                        }
                    }
                    //horizontal border
                    //vertical border

                    for (int w = -spread + 1; w < spread; w++) {
                        SeaTile border = map.getSeaTile(x + w, y - spread);
                        if (border != null && border.isWater()) {
                            biomass.set(x + w, y - spread,
                                        Math.min(biomass.get(x+w,y-spread) + Math.pow(smoothingValue, spread) * peakBiomass,peakBiomass));

                        }
                        border = map.getSeaTile(x + w, y + spread);
                        if (border != null && border.isWater()) {
                            biomass.set(x + w, y + spread,
                                        Math.min(biomass.get(x+w,y+spread) + Math.pow(smoothingValue, spread) * peakBiomass,peakBiomass));
                        }
                    }

                }
            }
        }





        assert biomass!=null;
        return biomass.get(tile.getGridX(),
                           tile.getGridY());

    }
}
