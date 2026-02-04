/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.grids;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.ToString;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Envelope;

import java.util.Arrays;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

class ModelGridWithInactiveCells extends AbstractModelGrid {

    // Using ImmutableSet here as it should provide fast lookup _and_ iteration
    @ToString.Exclude
    @Getter
    private final ImmutableSet<Int2D> activeCells;

    ModelGridWithInactiveCells(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Predicate<Int2D> activePredicate
    ) {
        this(
            gridWidth,
            gridHeight,
            envelope,
            makeAllCellsArray(gridWidth, gridHeight),
            activePredicate
        );
    }

    private ModelGridWithInactiveCells(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Int2D[] allCells,
        final Predicate<Int2D> activePredicate
    ) {
        this(
            gridWidth,
            gridHeight,
            envelope,
            allCells,
            Arrays.stream(allCells).filter(activePredicate).collect(toImmutableSet())
        );
    }

    private ModelGridWithInactiveCells(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope,
        final Int2D[] allCells,
        final ImmutableSet<Int2D> activeCells
    ) {
        super(gridWidth, gridHeight, envelope, allCells);
        this.activeCells = activeCells;
    }

    @Override
    public boolean isActive(final Int2D cell) {
        return activeCells.contains(cell);
    }

}
