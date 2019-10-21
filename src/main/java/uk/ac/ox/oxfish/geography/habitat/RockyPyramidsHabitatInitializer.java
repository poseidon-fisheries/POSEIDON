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

package uk.ac.ox.oxfish.geography.habitat;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * Everythin is sandy except for peaks of rockyness with some interpolation around the peaks
 * Created by carrknight on 10/1/15.
 */
public class RockyPyramidsHabitatInitializer implements HabitatInitializer {



    private final int numberOfPeaks;


    private final DoubleParameter smoothingValue;

    private final int maxSpread;

    public RockyPyramidsHabitatInitializer(int numberOfPeaks, DoubleParameter smoothingValue, int maxSpread) {
        this.numberOfPeaks = numberOfPeaks;
        this.smoothingValue = smoothingValue;
        this.maxSpread = maxSpread;
    }

    @Override
    public void applyHabitats(
            NauticalMap map, MersenneTwisterFast random, FishState model) {


        final int mapHeight = map.getHeight();
        final int mapWidth = map.getWidth();

        for(int i=0; i< numberOfPeaks; i++) {

            //create the bottom left corner
            int x;
            int y;
            do {
                x = random.nextInt(mapWidth);
                y = random.nextInt(mapHeight);
            }
            while (map.getSeaTile(x, y).isLand());


            map.getSeaTile(x,y).setHabitat(new TileHabitat(1d));

            for(int spread = 1; spread<maxSpread; spread++)
            {


                //vertical border
                for(int h=-spread; h<=spread; h++)
                {
                    SeaTile border = map.getSeaTile(x-spread, y + h);
                    if(border != null && border.isWater())
                    {

                        border.setHabitat(new TileHabitat(
                                Math.min(border.getRockyPercentage() +
                                                 Math.pow(smoothingValue.apply(random),spread), 1)


                        ));
                    }
                    border = map.getSeaTile(x+spread, y + h);
                    if(border != null && border.isWater())
                    {
                        border.setHabitat(new TileHabitat(
                                Math.min(border.getRockyPercentage() +
                                                 Math.pow(smoothingValue.apply(random),spread), 1)


                        ));
                    }
                }
                //horizontal border
                //vertical border

                for(int w=-spread+1; w<spread; w++)
                {
                    SeaTile border = map.getSeaTile(x+w, y - spread);
                    if(border != null && border.isWater())
                    {
                        border.setHabitat(new TileHabitat(
                                Math.min( border.getRockyPercentage() + Math.pow(smoothingValue.apply(random),spread),1)


                        ));
                    }
                    border = map.getSeaTile(x+w, y+spread);
                    if(border != null && border.isWater())
                    {
                        border.setHabitat(new TileHabitat(
                                Math.min( border.getRockyPercentage() +
                                                  Math.pow(smoothingValue.apply(random),spread),1)


                        ));
                    }
                }



            }



        }

        }




}
