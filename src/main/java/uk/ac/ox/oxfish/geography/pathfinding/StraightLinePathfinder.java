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

package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

/**
 * The default pathfinder: quick and stupid as it runs over land and everything else
 * Created by carrknight on 11/4/15.
 */
public class StraightLinePathfinder implements Pathfinder {

    private final PathMemory memory;

    public StraightLinePathfinder() { this(new TableBasedPathMemory()); }

    StraightLinePathfinder(PathMemory memory) { this.memory = memory; }

    /**
     * builds a osmoseWFSPath from start to end. No weird pathfinding here, simply move diagonally then horizontally-vertically when that's not possible anymore
     *
     * @param map   the nautical map
     * @param start the start point
     * @param end   the end point
     * @return a queue of tiles to pass through form start to end. Empty if starting point is the ending point
     */
    @SuppressWarnings("OptionalAssignedToNull")
    public Deque<SeaTile> getRoute(NauticalMap map, SeaTile start, SeaTile end) {

        assert start != null : "start " + start + ", end: " + end;
        assert end != null : "start " + start + ", end: " + end;

        int x = start.getGridX();
        int endX = end.getGridX();
        int y = start.getGridY();
        int endY = end.getGridY();

        // If we already have this path in our memory, return a mutable copy of it
        final Optional<ImmutableList<SeaTile>> knownPath = memory.getPath(start, end);
        if (knownPath != null) return knownPath.map(LinkedList::new).orElse(null);

        final Deque<SeaTile> path = new LinkedList<>();
        path.add(start);

        while (x != endX || y != endY) {

            int candidateX = x + Integer.signum(endX - x);
            int candidateY = y + Integer.signum(endY - y);

            //can you move your preferred way?
            SeaTile bestSeaTile = map.getSeaTile(candidateX, candidateY);
            if (bestSeaTile.isWater() || bestSeaTile.isPortHere()) {
                path.add(bestSeaTile);
                x = candidateX;
                y = candidateY;
            }
            //try to move on the x axis only then
            else if (candidateX != x && map.getSeaTile(candidateX, y).isWater()) {
                x = candidateX;
                path.add(map.getSeaTile(candidateX, y));
            } else if (candidateY != y && map.getSeaTile(x, candidateY).isWater()) {
                y = candidateY;
                path.add(map.getSeaTile(x, candidateY));

            }
            //otherwise just go over land!
            else {
                path.add(map.getSeaTile(candidateX, candidateY));
                x = candidateX;
                y = candidateY;
            }
        }

        assert Objects.equals(path.peekLast(), end);
        assert Objects.equals(path.peekFirst(), start);

        memory.putPath(start, end, path);

        return path;

    }

}
