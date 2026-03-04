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

import com.google.common.util.concurrent.AtomicDouble;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.allocators.Allocator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.isNaN;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
abstract class AbstractDoubleGridFromAllocatorFactory<S extends Scope, G extends DoubleGrid>
    extends RelativeScopeFactory<S, G> {

    private Factory<? super S, ? extends ModelGrid> modelGrid;
    private Factory<? super S, ? extends Allocator> allocator;

    @Override
    protected G newInstance(final S scope) {
        final ModelGrid modelGrid = this.modelGrid.get(scope);
        final Allocator allocator = this.allocator.get(scope);
        final double[][] values = modelGrid.makeDoubleArray();
        final AtomicDouble sum = new AtomicDouble(0);
        modelGrid.getAllCells().forEach(cell -> {
            final double value = allocator.applyAsDouble(cell);
            if (!isNaN(value)) {
                checkArgument(value >= 0, "Allocator returned negative value at %s", cell);
                sum.addAndGet(value);
            }
            values[cell.x][cell.y] = value;
        });
        return makeGrid(modelGrid, postProcess(scope, values, sum.doubleValue()));
    }

    protected double[][] postProcess(
        final S scope,
        final double[][] values,
        final double sum
    ) {
        return values;
    }

    abstract G makeGrid(
        final ModelGrid modelGrid,
        final double[][] values
    );
}
