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

package uk.ac.ox.poseidon.geography.bathymetry;

import lombok.*;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.AbstractFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.Collection;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BathymetricGridFactory<S extends Scope>
    extends AbstractFactory<S, BathymetricGrid> {

    @NonNull private Factory<? super S, ? extends ModelGrid> modelGrid;
    @NonNull private Factory<? super S, ? extends Aggregator> aggregator;
    @Builder.Default private boolean inverted = false;

    @Override
    protected BathymetricGrid newInstance(final S scope) {
        final ModelGrid modelGrid = this.modelGrid.get(scope);
        final Aggregator aggregator = this.aggregator.get(scope);
        final Map<Int2D, Collection<Double>> elevationValues = readElevationValues(
            modelGrid,
            scope
        );
        final double[][] array = modelGrid.makeDoubleArray();
        modelGrid.getAllCells().forEach(int2D ->
            array[int2D.x][int2D.y] = aggregator.apply(elevationValues.get(int2D)).orElse(0)
        );
        return new DefaultBathymetricGrid(modelGrid, array);
    }

    protected abstract Map<Int2D, Collection<Double>> readElevationValues(
        ModelGrid modelGrid,
        S scope
    );
}
