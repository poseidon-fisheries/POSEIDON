package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Optional;

public class TableBasedPathMemory implements PathMemory {

    private final Table<SeaTile, SeaTile, Optional<ImmutableList<SeaTile>>> memory = HashBasedTable.create();

    /**
     * Returns a path from start to end if one is known.
     * Otherwise, checks for a path from end to start that we can reverse, storing it and returning it if there is one.
     * Returned paths are wrapped in Optional. An empty optional means the path is impossible.
     *
     * @param start The SeaTile at which the path should start
     * @param end   The SeaTile at which the path should end
     * @return Either:
     * <ul>
     * <li>An immutable list of sea tiles wrapped in an Optional if there is a known path between start and end;</li>
     * <li>An empty Optional if we know there is no path between start and end;</li>
     * <li>null if we don't know anything about this path.</li>
     * </ul>
     */
    @Override
    @SuppressWarnings("OptionalAssignedToNull")
    public Optional<ImmutableList<SeaTile>> getPath(SeaTile start, SeaTile end) {
        final Optional<ImmutableList<SeaTile>> knownPath = memory.get(start, end);
        if (knownPath != null)
            return knownPath;
        else {
            // maybe we have it in reverse?
            final Optional<ImmutableList<SeaTile>> knownInversePath = memory.get(end, start);
            if (knownInversePath != null) {
                // We do! Make a new path by reversing it back to the direction we want, memorise it, and return it.
                final Optional<ImmutableList<SeaTile>> newPath = knownInversePath.map(ImmutableList::reverse);
                memory.put(start, end, newPath);
                return newPath;
            }
        }
        return null;
    }

    @Override
    public void putPath(SeaTile start, SeaTile end, ImmutableList<SeaTile> path) {
        memory.put(start, end, Optional.of(path));
    }

    @Override
    public void putImpossiblePath(SeaTile start, SeaTile end) {
        memory.put(start, end, Optional.empty());
    }

}
