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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.allocators.Allocator;

import java.util.function.Predicate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
abstract class AbstractDoubleGridFromAllocatorFactory<S extends Scope, G extends DoubleGrid>
    extends RelativeScopeFactory<S, G> {

    private Factory<? super S, ? extends ModelGrid> modelGrid;
    private Factory<? super S, ? extends Allocator> allocator;
    private Factory<? super S, ? extends Predicate<Int2D>> cellPredicate;

    @Override
    protected G newInstance(final S scope) {
        final ModelGrid modelGrid = this.modelGrid.get(scope);
        final Allocator allocator = this.allocator.get(scope);
        final Predicate<Int2D> cellPredicate = this.cellPredicate.get(scope);
        final double[][] values = modelGrid.makeDoubleArray();
        modelGrid.getAllCells().forEach(cell ->
            values[cell.x][cell.y] = cellPredicate.test(cell)
                ? allocator.applyAsDouble(cell)
                : Double.NaN
        );
        return makeGrid(modelGrid, values);
    }

    abstract G makeGrid(
        final ModelGrid modelGrid,
        final double[][] values
    );
}
