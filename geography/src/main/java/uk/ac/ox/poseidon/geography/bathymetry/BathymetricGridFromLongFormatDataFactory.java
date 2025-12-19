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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BathymetricGridFromLongFormatDataFactory<S extends Scope>
    extends BathymetricGridFactory<S> {

    private Factory<? super S, ? extends Table> data;

    @NonNull private String longitudeColumn;
    @NonNull private String latitudeColumn;
    @NonNull private String depthColumn;

    public BathymetricGridFromLongFormatDataFactory(
        @NonNull final Factory<? super S, ? extends Path> path,
        @NonNull final Factory<? super S, ? extends ModelGrid> modelGrid,
        @NonNull final Factory<? super S, ? extends Aggregator> aggregator,
        final boolean inverted,
        @NonNull final String longitudeColumn,
        @NonNull final String latitudeColumn,
        @NonNull final String depthColumn
    ) {
        super(modelGrid, aggregator, inverted);
        this.longitudeColumn = longitudeColumn;
        this.latitudeColumn = latitudeColumn;
        this.depthColumn = depthColumn;
    }

    @Override
    protected Map<Int2D, Collection<Double>> readElevationValues(
        final ModelGrid modelGrid,
        final S scope
    ) {
        final Multimap<Int2D, Double> elevationValues = ArrayListMultimap.create();
        data.get(scope).forEach(row -> {
            final Int2D cell =
                modelGrid.toCell(new Coordinate(
                    row.getDouble(longitudeColumn),
                    row.getDouble(latitudeColumn)
                ));
            final double value = row.getDouble(depthColumn);
            elevationValues.put(cell, isInverted() ? -value : value);
        });
        return elevationValues.asMap();
    }

}
