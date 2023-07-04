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

package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Holds on to the last day any group of the map was last visited
 * Created by carrknight on 12/12/16.
 */
public class DiscretizedLocationMemory {


    private final MapDiscretization discretization;

    private final List<LinkedList<Integer>> visits;


    public DiscretizedLocationMemory(final MapDiscretization discretization) {
        this.discretization = discretization;
        this.visits = Stream
            .generate(LinkedList<Integer>::new)
            .limit(discretization.getNumberOfGroups())
            .collect(toList());
    }

    public void registerVisit(final int group, final int day) {
        visits.get(group).add(day);
    }

    public void registerVisit(final SeaTile tile, final int day) {
        final Integer group = discretization.getGroup(tile);
        if (group != null)
            visits.get(group).add(day);
    }


    public int getLastDayVisited(final int group) {
        return visits.get(group).isEmpty() ? -10000 : visits.get(group).getLast();
    }

    public LinkedList<Integer> getVisits(final int group) {
        return visits.get(group);
    }

}
