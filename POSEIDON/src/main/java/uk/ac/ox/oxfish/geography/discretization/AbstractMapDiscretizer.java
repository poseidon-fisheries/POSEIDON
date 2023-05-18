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

package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract discretizer, takes care of the filtering part of the Discretizer design
 * Created by carrknight on 7/17/17.
 */
public abstract class AbstractMapDiscretizer implements MapDiscretizer {

    /**
     * this predicate initially returns always true, but can be augmented by additional predicates
     */
    private Predicate<SeaTile> filter = new Predicate<SeaTile>() {
        @Override
        public boolean test(SeaTile tile) {
            return true;
        }
    };


    /**
     * assign all tiles to an array of groups (all groups must be disjoint)
     *
     * @param map the map to discretize
     * @return an array of lists, each list representing a group.
     */
    @Override
    public List<SeaTile>[] discretize(NauticalMap map) {

        return discretize(
            map,
            map.getAllSeaTilesExcludingLandAsList().stream().filter(filter).collect(Collectors.toList())
        );
    }

    /**
     * return groups but only for seatiles in the tiles list (which is all the seatiles we consider valid)
     *
     * @param map           the nautical map
     * @param validSeatiles the list of valid seatiles
     * @return groups
     */
    public abstract List<SeaTile>[] discretize(NauticalMap map, List<SeaTile> validSeatiles);

    /**
     * If any filter returns false, the seatile is not meant to be grouped
     *
     * @param filter
     */
    @Override
    public void addFilter(Predicate<SeaTile> filter) {
        this.filter = filter.and(filter);
    }
}
