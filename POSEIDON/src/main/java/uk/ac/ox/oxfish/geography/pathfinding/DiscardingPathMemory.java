package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;
import java.util.Optional;

/**
 * This class provides a way to prevent a Pathfinder from remembering anything.
 */
public class DiscardingPathMemory implements PathMemory {

    @SuppressWarnings("OptionalAssignedToNull")
    @Override public @Nullable Optional<ImmutableList<SeaTile>> getPath(SeaTile start, SeaTile end) { return null; }

    @Override public void putPath(SeaTile start, SeaTile end, Collection<SeaTile> path) {} // do nothing

    @Override public void putPath(SeaTile start, SeaTile end, ImmutableList<SeaTile> path) {} // do nothing

    @Override public void putImpossiblePath(SeaTile start, SeaTile end) {} // do nothing
}
