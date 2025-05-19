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
