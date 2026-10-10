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

package uk.ac.ox.poseidon.agents.choices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link ConstantDestinationSupplier}, built via
 * {@link Factories#constantDestination}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConstantDestinationSupplierFactory<S extends Scope>
    extends RelativeScopeFactory<S, DestinationSupplier> {

    private Factory<? super S, ? extends ModelGrid> modelGrid;
    private Factory<? super S, ? extends Coordinate> coordinate;

    /**
     * @return a {@link ConstantDestinationSupplier} for {@link #coordinate}'s cell in {@link #modelGrid}
     * @throws IllegalArgumentException if {@link #coordinate} falls outside {@link #modelGrid}
     */
    @Override
    protected DestinationSupplier newInstance(final S scope) {
        final ModelGrid modelGrid = this.modelGrid.get(scope);
        final Coordinate coordinate = this.coordinate.get(scope);
        modelGrid.checkIsInGrid(coordinate);
        return new ConstantDestinationSupplier(modelGrid.toCell(coordinate));
    }
}
