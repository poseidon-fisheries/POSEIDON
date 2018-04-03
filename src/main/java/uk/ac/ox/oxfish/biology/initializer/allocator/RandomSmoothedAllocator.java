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
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;


/**
 * lazily initialize allocator
 */
public class RandomSmoothedAllocator implements BiomassAllocator {


    private final double absoluteMaximum;

    private final double absoluteMinimum;

    private final int smoothingRuns;


    private double[][] biomassMap;
    private final double aggressivness;


    public RandomSmoothedAllocator(
            double absoluteMaximum, double absoluteMinimum, int smoothingRuns, double aggressivness) {
        this.absoluteMaximum = absoluteMaximum;
        this.absoluteMinimum = absoluteMinimum;
        this.smoothingRuns = smoothingRuns;
        this.aggressivness = aggressivness;
    }

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

        //lazy initialization
        lazyInitialization(map, random);


        return biomassMap[tile.getGridX()][tile.getGridY()];

    }

    public void lazyInitialization(NauticalMap map, MersenneTwisterFast random) {
        if(biomassMap==null)
        {
            int width = map.getWidth();
            int height = map.getHeight();
            biomassMap = new double[width][height];
            for(int x=0; x<biomassMap.length; x++)
                for(int y=0; y<biomassMap[0].length; y++)
                    biomassMap[x][y] = random.nextDouble(true,true)*
                            (absoluteMaximum-absoluteMinimum) + absoluteMinimum;

            /***
             *      ___                _   _    _
             *     / __|_ __  ___  ___| |_| |_ (_)_ _  __ _
             *     \__ \ '  \/ _ \/ _ \  _| ' \| | ' \/ _` |
             *     |___/_|_|_\___/\___/\__|_||_|_|_||_\__, |
             *                                        |___/
             */


            for(int i=0; i<smoothingRuns; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                int xNew = x + random.nextInt(3) - 1;
                xNew = Math.max(0, xNew);
                xNew = Math.min(xNew, width - 1);
                int yNew = y + random.nextInt(3) - 1;
                yNew = Math.max(0, yNew);
                yNew = Math.min(yNew, height - 1);
                double newValue = biomassMap[x][y] +
                        (random.nextDouble() * aggressivness) *
                                (biomassMap[xNew][yNew] - biomassMap[x][y]);
                if (newValue > absoluteMaximum)
                    newValue = absoluteMaximum;
                if (newValue < absoluteMinimum)
                    newValue = absoluteMinimum;


                biomassMap[x][y] = newValue;
            }
            assert biomassMap!=null;


        }
    }
}
