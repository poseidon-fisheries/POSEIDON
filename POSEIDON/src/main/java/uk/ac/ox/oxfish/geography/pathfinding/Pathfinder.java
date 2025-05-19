/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography.pathfinding;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;

/**
 * An object tasked to return a full osmoseWFSPath from start to end
 * Created by carrknight on 11/4/15.
 */
public interface Pathfinder {


    /**
     * return the full osmoseWFSPath that brings us from start to end
     *
     * @param map   the map
     * @param start the starting tile
     * @param end   the ending tile
     * @return a queue of steps from start to end or null if it isn't possible to go from start to end
     */
    Deque<SeaTile> getRoute(NauticalMap map, SeaTile start, SeaTile end);


}
