/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.utils.ElevationTable;

import java.util.Collection;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BathymetricGridFromElevationTableFactory<S extends Scope>
    extends BathymetricGridFactory<S> {

    private Factory<? super S, ? extends ElevationTable> elevationTable;

    public BathymetricGridFromElevationTableFactory(
        @NonNull final Factory<? super S, ? extends ElevationTable> elevationTable,
        @NonNull final Factory<? super S, ? extends ModelGrid> modelGrid,
        @NonNull final Factory<? super S, ? extends Aggregator> aggregator,
        final boolean inverted
    ) {
        super(modelGrid, aggregator, inverted);
        this.elevationTable = elevationTable;
    }

    @Override
    protected Map<Int2D, Collection<Double>> readElevationValues(
        final ModelGrid modelGrid,
        final S scope
    ) {
        final Multimap<Int2D, Double> elevationValues = ArrayListMultimap.create();
        elevationTable.get(scope).entryStream().forEach(entry -> {
            final Int2D cell = modelGrid.toCell(entry.getCoordinate());
            final double value = entry.getElevation();
            elevationValues.put(cell, isInverted() ? -value : value);
        });
        return elevationValues.asMap();
    }

}
