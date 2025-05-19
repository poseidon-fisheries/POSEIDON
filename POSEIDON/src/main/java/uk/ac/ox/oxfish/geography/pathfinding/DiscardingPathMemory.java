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
