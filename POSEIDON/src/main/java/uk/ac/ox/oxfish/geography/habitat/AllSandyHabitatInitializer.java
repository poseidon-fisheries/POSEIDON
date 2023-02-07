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
