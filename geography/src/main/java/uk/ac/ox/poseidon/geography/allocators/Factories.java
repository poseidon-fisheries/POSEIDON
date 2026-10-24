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

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * Factories for {@link Allocator}s: weight functions used to place things on a
 * grid.
 */
@SuppressWarnings("overloads")
public final class Factories {

  private Factories() {}

  /**
   * @param delegateAllocator factory for the allocator to delegate to for
   *                          matching cells
   * @param cellPredicate     factory for the predicate deciding which cells are
   *                          allocated to
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link FilteredAllocator}
   * @see FilteredAllocator
   */
  public static <S extends Scope> FilteredAllocatorFactory<S> filteredAllocator(
    final Factory<? super S, ? extends Allocator> delegateAllocator,
    final MappingFactory<
      ? super S,
      ? super Void,
      ? super Cell,
      Boolean
    > cellPredicate
  ) {
    return new FilteredAllocatorFactory<>(delegateAllocator, cellPredicate);
  }

  /**
   * @param value every cell's weight, asked with no context
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link SupplierAllocator}
   * @see SupplierAllocator
   */
  public static <S extends Scope> SupplierAllocatorFactory<S> supplierAllocator(
    final ValueFactory<? super S, ? super Void, Double> value
  ) {
    return new SupplierAllocatorFactory<>(value);
  }

  /**
   * @param value the value allocated to every cell
   * @return the same as {@link #supplierAllocator(ValueFactory)}, with a
   *         constant {@code value}
   */
  public static <S extends Scope> SupplierAllocatorFactory<S> supplierAllocator(
    final double value
  ) {
    return supplierAllocator(constant(value));
  }
}
