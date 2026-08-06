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

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.file.Path;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

/**
 * Reads a NetCDF file holding one static {@code (latitude, longitude)} raster per data variable,
 * on a grid that must align cell-for-cell with the given {@link ModelGrid} (no resampling is
 * performed; misalignment is an error), and exposes the result as an immutable map keyed by the
 * raw NetCDF variable name. Unlike species-biomass reading, there is no closed, known-in-advance
 * set of names to match against: this is a generic reader, equally usable for MPA membership
 * masks (one variable per MPA id) or, later, habitat-type grids — whatever a static NetCDF file
 * happens to contain.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StaticGridsFromNetCdfFactory<S extends Scope>
    extends RelativeScopeFactory<S, ImmutableMap<String, DoubleGridWrapper>> {

    private Factory<? super S, ? extends ModelGrid> modelGrid;
    private Factory<? super S, ? extends Path> ncFilePath;
    private String latitudeDimensionName;
    private String longitudeDimensionName;

    @Override
    protected ImmutableMap<String, DoubleGridWrapper> newInstance(final S scope) {

        final ModelGrid modelGrid = this.modelGrid.get(scope);

        try (final StaticNetCdfGridReader netCdfGridReader = new StaticNetCdfGridReader(
            ncFilePath.get(scope),
            latitudeDimensionName,
            longitudeDimensionName
        )) {

            netCdfGridReader.checkAlignmentWith(modelGrid);

            return netCdfGridReader
                .getDataVariableNames()
                .stream()
                .collect(toImmutableMap(
                    identity(),
                    variableName -> new DoubleGridWrapper(
                        new BaseDoubleGrid(modelGrid, netCdfGridReader.readGrid(variableName))
                    )
                ));
        }
    }

}
