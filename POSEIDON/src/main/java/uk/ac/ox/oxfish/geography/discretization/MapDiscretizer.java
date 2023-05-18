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

/**
 * Any object that allocates all seatiles to a group or another
 * Created by carrknight on 11/30/16.
 */
public interface MapDiscretizer {


    /**
     * assign all tiles to an array of groups (all groups must be disjoint)
     *
     * @param map the map to discretize
     * @return an array of lists, each list representing a group.
     */
    public List<SeaTile>[] discretize(NauticalMap map);

    /**
     * If any filter returns false, the seatile is not meant to be grouped
     *
     * @param filter
     */
    public void addFilter(Predicate<SeaTile> filter);

}
