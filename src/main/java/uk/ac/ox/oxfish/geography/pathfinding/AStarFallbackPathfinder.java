package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

/**
 * This class first tries to find a straight line path, checks if it goes over land
 * and falls back on A* when that is the case. It uses a memory-less {@link StraightLinePathfinder}
 * and shares its own memory with the {@link AStarPathfinder} instance.
 */
public class AStarFallbackPathfinder implements Pathfinder {

    private final PathMemory memory;

    private final Pathfinder straightLinePathfinder = new StraightLinePathfinder(new DiscardingPathMemory());
    private final AStarPathfinder aStarPathfinder;

    public AStarFallbackPathfinder(Distance distanceFunction) { this(distanceFunction, new TableBasedPathMemory()); }

    @SuppressWarnings("WeakerAccess")
    public AStarFallbackPathfinder(Distance distanceFunction, PathMemory memory) {
        this.memory = memory;
        this.aStarPathfinder = new AStarPathfinder(distanceFunction, memory);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Override public Deque<SeaTile> getRoute(NauticalMap map, SeaTile start, SeaTile end) {

        // If we already have this path in our memory, return a mutable copy of it
        final Optional<ImmutableList<SeaTile>> knownPath = memory.getPath(start, end);
        if (knownPath != null) return knownPath.map(LinkedList::new).orElse(null);

        // We don't know that path, see if we can get a straight path
        final Deque<SeaTile> straightPath = straightLinePathfinder.getRoute(map, start, end);
        final boolean pathCrossesLand = straightPath.stream().anyMatch(seaTile ->
            seaTile.getAltitude() >= 0 && !seaTile.isPortHere()
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
