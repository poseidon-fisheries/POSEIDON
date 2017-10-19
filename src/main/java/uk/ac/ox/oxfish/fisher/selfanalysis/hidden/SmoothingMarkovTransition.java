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

package uk.ac.ox.oxfish.fisher.selfanalysis.hidden;

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Quick smoothing transition: sums up the same number to every cell and then renormalizes.
 * This way we don't compute neighborhoods.
 * Created by carrknight on 6/27/16.
 */
public class SmoothingMarkovTransition implements  MarkovTransition
{

    /**
     * the higher the quicker it all normalizes to 1
     */
    private final double noiseValue;

    /**
     * needed to check what part of the map is actually sea
     */
    private final NauticalMap map;


    public SmoothingMarkovTransition(double noiseValue, NauticalMap map) {
        this.noiseValue = noiseValue;
        this.map = map;
    }

    @Override
    public MarkovBelief step(MarkovBelief current) {

        double sum = 0;
        //first pass
        for(SeaTile tile : map.getAllSeaTilesAsList())
            sum+= current.getBelief(tile) + noiseValue;

        //now again
        DoubleGrid2D newGrid = current.representBeliefs();
        for(SeaTile tile : map.getAllSeaTilesAsList())
            newGrid.set(tile.getGridX(),tile.getGridY(),
                        (newGrid.get(tile.getGridX(),tile.getGridY())+noiseValue)/sum);

        return new ExactMarkovBelief(newGrid);

    }

    /**
     * Getter for property 'noiseValue'.
     *
     * @return Value for property 'noiseValue'.
     */
    public double getNoiseValue() {
        return noiseValue;
    }

    /**
     * Getter for property 'map'.
     *
     * @return Value for property 'map'.
     */
    public NauticalMap getMap() {
        return map;
    }
}
