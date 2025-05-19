/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class first tries to find a straight line path, checks if it goes over land
 * and falls back on A* when that is the case. It uses a memory-less {@link StraightLinePathfinder}
 * and shares its own memory with the {@link AStarPathfinder} instance.
 */
public class AStarFallbackPathfinder implements Pathfinder {

    private final PathMemory memory;

    private final Pathfinder straightLinePathfinder = new StraightLinePathfinder(new DiscardingPathMemory());
    private final AStarPathfinder aStarPathfinder;

    public AStarFallbackPathfinder(Distance distanceFunction) {
        this(distanceFunction, new TableBasedPathMemory());
    }

    @SuppressWarnings("WeakerAccess")
    public AStarFallbackPathfinder(Distance distanceFunction, PathMemory memory) {
        this.memory = memory;
        this.aStarPathfinder = new AStarPathfinder(distanceFunction, memory);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Override
    public Deque<SeaTile> getRoute(NauticalMap map, SeaTile start, SeaTile end) {

        checkArgument(start.isWater() || start.isPortHere());
        checkArgument(end.isWater() || end.isPortHere());

        // If we already have this path in our memory, return a mutable copy of it
        final Optional<ImmutableList<SeaTile>> knownPath = memory.getPath(start, end);
        if (knownPath != null) return knownPath.map(LinkedList::new).orElse(null);

        // We don't know that path, see if we can get a straight path
        final Deque<SeaTile> straightPath = straightLinePathfinder.getRoute(map, start, end);
        final boolean pathCrossesLand = straightPath.stream().anyMatch(seaTile ->
            seaTile.isLand() && !seaTile.isPortHere()
        );
        if (!pathCrossesLand) {
            // We have a straight path across water, put it in memory and return it
            memory.putPath(start, end, straightPath);
            return straightPath;
        }
        // We don't know the path yet, and there is no straight path, so we delegate the job to the AStarPathfinder,
        // which will take care of storing the path it finds in our shared memory.
        return aStarPathfinder.getRoute(map, start, end);
    }
}
