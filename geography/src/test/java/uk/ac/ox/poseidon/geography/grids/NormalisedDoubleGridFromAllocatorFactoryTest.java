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

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.allocators.FilteredAllocator;

import static java.lang.Double.isNaN;
import static org.assertj.core.api.Assertions.*;

class NormalisedDoubleGridFromAllocatorFactoryTest {

    @Test
    void normalisesToTargetTotalAndKeepsNaNs() {
        final ModelGrid modelGrid = ModelGrid.create(2, 2, new Envelope(0, 2, 0, 2));
        final NormalisedDoubleGridFromAllocatorFactory<Scope> factory =
            new NormalisedDoubleGridFromAllocatorFactory<>(
                scope -> modelGrid,
                scope -> new FilteredAllocator(
                    cell -> 1.0,
                    cell -> !(cell.x == 0 && cell.y == 0)
                ),
                scope -> 12.0
            );

        final BaseDoubleGrid grid = factory.get(Scope.GLOBAL_SCOPE);
        double sum = 0;
        int nanCount = 0;
        for (final Int2D cell : modelGrid.getAllCells().toList()) {
            final double value = grid.getValue(cell);
            if (isNaN(value)) {
                nanCount++;
            } else {
                sum += value;
            }
        }

        assertThat(sum).isCloseTo(12.0, within(1e-9));
        assertThat(nanCount).isEqualTo(1);
        assertThat(grid.getValue(new Int2D(1, 1))).isCloseTo(4.0, within(1e-9));
    }

    @Test
    void rejectsNegativeAllocatorValues() {
        final ModelGrid modelGrid = ModelGrid.create(2, 2, new Envelope(0, 2, 0, 2));
        final NormalisedDoubleGridFromAllocatorFactory<Scope> factory =
            new NormalisedDoubleGridFromAllocatorFactory<>(
                _ -> modelGrid,
                _ -> cell -> cell.x == 1 ? -1.0 : 1.0,
                _ -> 4.0
            );

        assertThatThrownBy(() -> factory.get(Scope.GLOBAL_SCOPE))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
