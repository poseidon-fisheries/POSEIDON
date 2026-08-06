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

import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import java.nio.file.Path;
import java.util.List;

/**
 * Reads a NetCDF file holding a single static {@code (latitude, longitude)} raster per data
 * variable — e.g. one 0/1 membership mask per named zone (MPA id, habitat type, ...).
 */
public final class StaticNetCdfGridReader extends AbstractNetCdfGridReader {

    public StaticNetCdfGridReader(
        final Path ncFile,
        final String latitudeDimensionName,
        final String longitudeDimensionName
    ) {
        super(ncFile, latitudeDimensionName, longitudeDimensionName);
    }

    /**
     * Names of the data variables shaped exactly {@code (latitude, longitude)}, excluding the
     * coordinate variables and any grid-mapping variable (e.g. {@code crs}).
     */
    public List<String> getDataVariableNames() {
        return netcdfFile
            .getVariables()
            .stream()
            .filter(this::isLatitudeLongitudeVariable)
            .map(Variable::getFullName)
            .toList();
    }

    private boolean isLatitudeLongitudeVariable(final Variable variable) {
        final List<Dimension> dimensions = variable.getDimensions();
        return dimensions.size() == 2
            && latitudeDimensionName.equals(dimensions.get(0).getShortName())
            && longitudeDimensionName.equals(dimensions.get(1).getShortName());
    }

    /**
     * Reads the named variable's {@code (latitude, longitude)} raster, reoriented to
     * {@code [longitudeIndex][latitudeIndex]}, with {@code _FillValue} cells converted to
     * {@link Double#NaN}.
     */
    public double[][] readGrid(final String variableName) {
        final Variable variable = findVariable(variableName);
        final int latDim = getLatDimensionSize();
        final int lonDim = getLonDimensionSize();
        return readAndReorient(variable, new int[]{0, 0}, new int[]{latDim, lonDim});
    }

}
