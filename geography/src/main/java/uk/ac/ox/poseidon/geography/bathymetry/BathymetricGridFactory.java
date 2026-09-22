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
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.Collection;
import java.util.Map;

/**
 * A {@link RelativeScopeFactory} base for building a {@link DefaultBathymetricGrid} from raw
 * elevation samples: subclasses supply the samples, grouped by grid cell (via
 * {@link #readElevationValues}, since a cell may receive several raw values, e.g. from an
 * unaligned source grid); this class handles resolving each cell's final elevation by applying
 * the resolved {@link Aggregator} to its sample values (an empty cell defaults to {@code 0}) and
 * optionally negating every value (via {@code inverted}, for sources where elevation and depth
 * are the opposite sign convention from this codebase's "negative is water" rule).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BathymetricGridFactory<S extends Scope>
    extends RelativeScopeFactory<S, BathymetricGrid> {

    @NonNull private Factory<? super S, ? extends ModelGrid> modelGrid;
    @NonNull private Factory<? super S, ? extends Aggregator> aggregator;
    private boolean inverted;

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

    /**
     * @param modelGrid the resolved grid to read elevation samples for
     * @param scope     the scope being resolved against
     * @return every raw elevation sample, grouped by the cell it belongs to (subclasses are
     * responsible for applying {@code inverted} to their own values)
     */
    protected abstract Map<Int2D, Collection<Double>> readElevationValues(
        ModelGrid modelGrid,
        S scope
    );
}
