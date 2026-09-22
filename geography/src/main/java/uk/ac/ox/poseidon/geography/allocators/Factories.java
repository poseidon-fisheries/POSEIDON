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

package uk.ac.ox.poseidon.geography.allocators;

import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

/** Factories for {@link Allocator}s: weight functions used to place things on a grid. */
public class Factories {

    private Factories() {}

    /**
     * @param delegateAllocator factory for the allocator to delegate to for matching cells
     * @param cellPredicate     factory for the predicate deciding which cells are allocated to
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link FilteredAllocator}
     * @see FilteredAllocator
     */
    public static <S extends Scope> FilteredAllocatorFactory<S> filteredAllocator(
        final Factory<? super S, ? extends Allocator> delegateAllocator,
        final Factory<? super S, ? extends Predicate<? super Int2D>> cellPredicate
    ) {
        return new FilteredAllocatorFactory<>(delegateAllocator, cellPredicate);
    }

    /**
     * @param doubleSupplier factory for the supplier providing every cell's weight
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link SupplierAllocator}
     * @see SupplierAllocator
     */
    public static <S extends Scope> SupplierAllocatorFactory<S> supplierAllocator(
        final Factory<? super S, ? extends DoubleSupplier> doubleSupplier
    ) {
        return new SupplierAllocatorFactory<>(doubleSupplier);
    }
}
