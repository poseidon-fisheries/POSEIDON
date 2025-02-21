/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.geography.bathymetry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import sim.util.Int2D;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class BathymetricGridFromLongFormatCsvFactory extends BathymetricGridFactory {

    @NonNull private String longitudeColumnName;
    @NonNull private String latitudeColumnName;
    @NonNull private String depthColumnName;

    public BathymetricGridFromLongFormatCsvFactory(
        @NonNull final Factory<? extends Path> path,
        @NonNull final Factory<? extends ModelGrid> modelGrid,
        @NonNull final Factory<? extends Aggregator> aggregator,
        final boolean inverted,
        @NonNull final String longitudeColumnName,
        @NonNull final String latitudeColumnName,
        @NonNull final String depthColumnName
    ) {
        super(path, modelGrid, aggregator, inverted);
        this.longitudeColumnName = longitudeColumnName;
        this.latitudeColumnName = latitudeColumnName;
        this.depthColumnName = depthColumnName;
    }

    @Override
    protected Map<Int2D, Collection<Double>> readElevationValues(
        final File gridFile,
        final ModelGrid modelGrid
    ) {
        final Multimap<Int2D, Double> elevationValues = ArrayListMultimap.create();
        Table.read().csv(gridFile).forEach(row -> {
            final Int2D cell =
                modelGrid.toCell(new Coordinate(
                    row.getDouble(longitudeColumnName),
                    row.getDouble(latitudeColumnName)
                ));
            final double value = row.getDouble(depthColumnName);
            elevationValues.put(cell, isInverted() ? -value : value);
        });
        return elevationValues.asMap();
    }

}
