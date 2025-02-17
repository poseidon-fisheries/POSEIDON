/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
import lombok.*;
import sim.util.Int2D;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import java.io.IOException;
import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BathymetricGridFromGebcoNetCdfGridFactory extends GlobalScopeFactory<BathymetricGrid> {
    @NonNull private Factory<? extends Path> path;
    @NonNull private Factory<? extends GridExtent> gridExtent;
    @NonNull private String latitudeVariableName = "lat";
    @NonNull private String longitudeVariableName = "lon";
    @NonNull private String elevationVariableName = "elevation";

    public BathymetricGridFromGebcoNetCdfGridFactory(
        @NonNull final Factory<? extends Path> path,
        @NonNull final Factory<? extends GridExtent> gridExtent
    ) {
        this.path = path;
        this.gridExtent = gridExtent;
    }

    @Override
    protected BathymetricGrid newInstance(final Simulation simulation) {
        final GridExtent gridExtent = this.gridExtent.get(simulation);
        final double[][] array = gridExtent.makeDoubleArray();
        readElevationValues(path.get(simulation), gridExtent)
            .asMap()
            .forEach((cell, elevations) -> {
                array[cell.x][cell.y] =
                    elevations.stream().mapToDouble(Short::doubleValue).average().orElse(0);
            });
        return new DefaultBathymetricGrid(gridExtent, array);
    }

    private Multimap<Int2D, Short> readElevationValues(
        final Path path,
        final GridExtent gridExtent
    ) {
        try (final NetcdfFile ncFile = NetcdfFiles.open(path.toString())) {
            final Variable latVar = findVariable(ncFile, latitudeVariableName);
            final Variable lonVar = findVariable(ncFile, longitudeVariableName);
            final Variable elevVar = findVariable(ncFile, elevationVariableName);

            final Array latArray = latVar.read();
            final Array lonArray = lonVar.read();
            final Array elevArray = elevVar.read();

            final int latSize = latVar.getShape()[0];
            final int lonSize = lonVar.getShape()[0];

            final Index elevIndex = elevArray.getIndex();

            final Multimap<Int2D, Short> elevationValues = ArrayListMultimap.create();
            for (int i = 0; i < latSize; i++) {
                final double lat = latArray.getDouble(i);
                for (int j = 0; j < lonSize; j++) {
                    final double lon = lonArray.getDouble(j);
                    elevIndex.set(i, j);
                    elevationValues.put(
                        gridExtent.toCell(new Coordinate(lon, lat)),
                        elevArray.getShort(elevIndex)
                    );
                }
            }
            return elevationValues;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Variable findVariable(
        final NetcdfFile ncFile,
        final String variableName
    ) {
        final Variable variable = ncFile.findVariable(variableName);
        if (variable == null) {
            throw new IllegalArgumentException(
                "Variable `%s` not found. Available variables are: %s".formatted(
                    variableName,
                    ncFile.getVariables().stream().map(Variable::getShortName).toList().toString()
                )
            );
        }
        return variable;
    }
}
