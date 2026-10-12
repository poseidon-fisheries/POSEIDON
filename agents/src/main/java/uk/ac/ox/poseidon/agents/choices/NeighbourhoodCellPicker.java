/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.choices;

import ec.util.MersenneTwisterFast;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.providers.Provider;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

/**
 * Picks a random accessible water cell in the neighbourhood of a search centre, asked for anew on
 * each pick (the vessel's own cell if it gives none), growing the search radius until a cell
 * satisfies the option predicate. The radius grows by at most {@code maximumWidening} cells
 * beyond the initial one, and never beyond the grid's larger dimension: when no cell is legal
 * anywhere, which is common (a closed port, no time left), an unbounded search would test every
 * water cell of the grid.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class NeighbourhoodCellPicker implements Provider<Int2D> {

    private final Vessel vessel;
    private final GridPathFinder pathFinder;
    private final Predicate<? super Int2D> optionPredicate;
    private final IntSupplier neighbourhoodSizeSupplier;
    private final int maximumWidening;
    private final Supplier<Int2D> searchCentre;
    private final MersenneTwisterFast rng;

    /**
     * @return a uniformly random accessible water cell satisfying the option predicate, within
     * the smallest searched neighbourhood that has any; {@code null} if none qualify within the
     * largest neighbourhood searched
     */
    @Override
    public Int2D get() {
        final int initialNeighbourhoodSize = neighbourhoodSizeSupplier.getAsInt();
        final int gridExtent = max(
            pathFinder.getModelGrid().getGridWidth(),
            pathFinder.getModelGrid().getGridHeight()
        );
        final int maxNeighbourhoodSize =
            initialNeighbourhoodSize + min(maximumWidening, gridExtent - initialNeighbourhoodSize);
        final Int2D suppliedCentre = searchCentre.get();
        final Int2D centre = suppliedCentre != null ? suppliedCentre : vessel.getCell();
        for (
            int neighbourhoodSize = initialNeighbourhoodSize;
            neighbourhoodSize <= maxNeighbourhoodSize;
            neighbourhoodSize++
        ) {
            final List<Int2D> neighbours =
                pathFinder.getAccessibleWaterNeighbours(centre, neighbourhoodSize);
            // Every cell of the previous neighbourhood has already failed the predicate
            final List<Int2D> candidates =
                neighbourhoodSize == initialNeighbourhoodSize
                    ? neighbours
                    : ring(neighbours, centre, neighbourhoodSize);
            final Int2D picked = shuffledStream(candidates, rng)
                .filter(optionPredicate)
                .findFirst()
                .orElse(null);
            if (picked != null) {
                return picked;
            }
        }
        return null;
    }

    /** @return the cells of {@code neighbours} exactly {@code size} cells away from the centre */
    private static List<Int2D> ring(
        final List<Int2D> neighbours,
        final Int2D centre,
        final int size
    ) {
        return neighbours
            .stream()
            .filter(cell -> max(abs(cell.x - centre.x), abs(cell.y - centre.y)) == size)
            .toList();
    }
}
