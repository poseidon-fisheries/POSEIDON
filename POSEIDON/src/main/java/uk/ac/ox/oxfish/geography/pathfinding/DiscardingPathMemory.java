package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;
import java.util.Optional;

/**
 * This class provides a way to prevent a Pathfinder from remembering anything.
 */
public class DiscardingPathMemory implements PathMemory {

    @SuppressWarnings("OptionalAssignedToNull")
    @Override
    public Optional<ImmutableList<SeaTile>> getPath(final SeaTile start, final SeaTile end) {
        return null;
    }

    @Override
    public void putPath(final SeaTile start, final SeaTile end, final Collection<SeaTile> path) {
    } // do nothing

    @Override
    public void putPath(final SeaTile start, final SeaTile end, final ImmutableList<SeaTile> path) {
    } // do nothing

    @Override
    public void putImpossiblePath(final SeaTile start, final SeaTile end) {
    } // do nothing
}
