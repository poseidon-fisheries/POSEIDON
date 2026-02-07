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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.allocators.Allocator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.isNaN;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
abstract class AbstractNormalisedDoubleGridFromAllocatorFactory<
    S extends Scope, G extends DoubleGrid
    > extends AbstractDoubleGridFromAllocatorFactory<S, G> {

    private Factory<? super S, ? extends Number> totalValue;

    protected AbstractNormalisedDoubleGridFromAllocatorFactory(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Allocator> allocator,
        final Factory<? super S, ? extends Number> totalValue
    ) {
        super(modelGrid, allocator);
        this.totalValue = totalValue;
    }

    @Override
    protected double[][] postProcess(
        final S scope,
        final double[][] values,
        final double sum
    ) {
        final double totalValue = this.totalValue.get(scope).doubleValue();
        checkArgument(totalValue > 0, "Total value must be positive");
        checkArgument(sum > 0, "Sum of values must be positive");
        final double scale = totalValue / sum;
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[x].length; y++) {
                if (!isNaN(values[x][y])) {
                    values[x][y] *= scale;
                }
            }
        }
        return values;
    }
}
