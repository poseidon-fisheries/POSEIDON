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

package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * hill climbs at one random direction until it can't climb further and pick that as its spot
 * Created by carrknight on 6/28/16.
 */
public class HillClimberAcquisitionFunction implements AcquisitionFunction {


    private final int stepSize;


    public HillClimberAcquisitionFunction(final int stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * The acquisition function main task: to pick a tile from the map given geographical regression
     *
     * @param map        the map to pick from
     * @param regression the geographical regression
     * @param state      @return a choice
     * @param fisher
     * @param current
     */
    @Override
    public SeaTile pick(
        final NauticalMap map,
        final GeographicalRegression<?> regression,
        final FishState state,
        final Fisher fisher,
        final SeaTile current
    ) {

        final double time = state.getHoursSinceStart();

        //start at a random location
        final List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        //start at current best if you have it
        SeaTile location = current == null ? tiles.get(state.getRandom().nextInt(tiles.size())) : current;
        Bag mooreNeighbors = new Bag(map.getMooreNeighbors(location, stepSize));
        mooreNeighbors.shuffle(state.getRandom());
        final Set<SeaTile> checkedAlready = new HashSet<>();
        //as long as there are neighbors you aren't done
        while (!mooreNeighbors.isEmpty()) {
            //remove a neighbor
            final SeaTile option = (SeaTile) mooreNeighbors.remove(0);
            //if it is better, restart search at that neighbor!
            if (option.isWater() && !checkedAlready.contains(option) &&
                regression.predict(location, time, fisher, state)
                    < regression.predict(option, time, fisher, state)) {
                location = option;
                mooreNeighbors = new Bag(map.getMooreNeighbors(location, stepSize));
                mooreNeighbors.shuffle(state.getRandom());
            }
            checkedAlready.add(option);
        }

        return location;
    }

    /**
     * Getter for property 'stepSize'.
     *
     * @return Value for property 'stepSize'.
     */
    public int getStepSize() {
        return stepSize;
    }
}
