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

package uk.ac.ox.poseidon.geography.grids.suppliers;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;

/**
 * Factories for {@link java.util.function.Supplier}s that aggregate a resolved
 * {@link DoubleGrid}.
 */
public class Factories {

    private Factories() {}

    /**
     * @param doubleGrid factory for the grid to sum
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link GridSumSupplier}
     * over the resolved grid
     * @see GridSumSupplier
     */
    public static <S extends Scope> GridSumSupplierFactory<S> gridSum(
        final Factory<? super S, ? extends DoubleGrid> doubleGrid
    ) {
        return new GridSumSupplierFactory<>(doubleGrid);
    }

}
