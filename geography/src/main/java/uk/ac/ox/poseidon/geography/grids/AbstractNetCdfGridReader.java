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

import sim.util.Int2D;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;

/**
 * Shared machinery for reading a NetCDF file on a regular latitude/longitude grid, exposing only
 * POSEIDON/JDK types (no {@code ucar.*} types leak out of this class). Reads via the raw,
 * non-"enhanced" CDM API so that {@code _FillValue} substitution is the only conversion applied —
 * CDM's automatic enhancement would also silently apply {@code scale_factor}/{@code add_offset}
 * /unit conversions that aren't wanted here. Subclasses add the shape-specific parts: which
 * variables count as data variables, and how a variable's values are sliced out.
 */
public abstract class AbstractNetCdfGridReader implements AutoCloseable {

    private static final String FILL_VALUE_ATTRIBUTE = "_FillValue";
    private static final double COORDINATE_EPSILON = 1e-6;

    protected final NetcdfFile netcdfFile;
    protected final String latitudeDimensionName;
    protected final String longitudeDimensionName;

    protected AbstractNetCdfGridReader(
        final Path ncFile,
        final String latitudeDimensionName,
        final String longitudeDimensionName
    ) {
        try {
            this.netcdfFile = NetcdfFiles.open(ncFile.toString());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.latitudeDimensionName = latitudeDimensionName;
        this.longitudeDimensionName = longitudeDimensionName;
    }

    public int getLonDimensionSize() {
        return findDimension(longitudeDimensionName).getLength();
    }

    public int getLatDimensionSize() {
        return findDimension(latitudeDimensionName).getLength();
    }

    /**
     * Longitude coordinate values (ascending, degrees_east), one per longitude cell.
     */
    public double[] getLongitudes() {
        return readDoubles(longitudeDimensionName);
    }

    /**
     * Latitude coordinate values (descending, degrees_north), one per latitude cell.
     */
    public double[] getLatitudes() {
        return readDoubles(latitudeDimensionName);
    }

    /**
     * Validates that this file's grid aligns exactly with the given {@code ModelGrid}, comparing
     * cell <em>centers</em> (via {@link ModelGrid#toCoordinate}) rather than raw envelope/cellsize
     * values, since those disagree at the 1e-7-1e-9 level between an {@code .asc} grid header and
     * a NetCDF {@code geotransform} even for grids that describe the same raster.
     */
    public void checkAlignmentWith(final ModelGrid modelGrid) {
        checkState(
            getLonDimensionSize() == modelGrid.getGridWidth()
                && getLatDimensionSize() == modelGrid.getGridHeight(),
            "NetCDF grid is %sx%s but ModelGrid is %sx%s",
            getLonDimensionSize(), getLatDimensionSize(),
            modelGrid.getGridWidth(), modelGrid.getGridHeight()
        );
        final double[] longitudes = getLongitudes();
        final double[] latitudes = getLatitudes();
        for (int lonIndex = 0; lonIndex < longitudes.length; lonIndex++) {
            for (int latIndex = 0; latIndex < latitudes.length; latIndex++) {
                final Coordinate expected = modelGrid.toCoordinate(new Int2D(lonIndex, latIndex));
                checkState(
                    Math.abs(expected.lon - longitudes[lonIndex]) < COORDINATE_EPSILON
                        && Math.abs(expected.lat - latitudes[latIndex]) < COORDINATE_EPSILON,
                    "NetCDF cell (lonIndex=%s, latIndex=%s) at (%s, %s) does not align with " +
                        "ModelGrid cell (%s, %s) at (%s, %s)",
                    lonIndex, latIndex, longitudes[lonIndex], latitudes[latIndex],
                    lonIndex, latIndex, expected.lon, expected.lat
                );
            }
        }
    }

    /**
     * Reads a {@code (latitude, longitude)}-shaped (or, with a leading size-1 dimension, e.g. a
     * single time slice, shaped {@code (1, latitude, longitude)}) slice of the given variable,
     * reoriented to {@code [longitudeIndex][latitudeIndex]} (i.e. {@code ModelGrid}'s
     * {@code [x][y]} convention), with {@code _FillValue} cells converted to {@link Double#NaN}.
     */
    protected double[][] readAndReorient(
        final Variable variable,
        final int[] origin,
        final int[] shape
    ) {
        final int latDim = getLatDimensionSize();
        final int lonDim = getLonDimensionSize();
        final float fillValue = fillValueOf(variable);

        final Array slice;
        try {
            slice = variable.read(origin, shape);
        } catch (final IOException | InvalidRangeException e) {
            throw new RuntimeException(e);
        }

        final double[][] values = new double[lonDim][latDim];
        for (int i = 0; i < latDim; i++) {
            for (int j = 0; j < lonDim; j++) {
                final float value = slice.getFloat(i * lonDim + j);
                values[j][i] = value == fillValue ? Double.NaN : value;
            }
        }
        return values;
    }

    private double[] readDoubles(final String variableName) {
        final Array array = readVariable(variableName);
        final double[] values = new double[(int) array.getSize()];
        for (int i = 0; i < values.length; i++) {
            values[i] = array.getDouble(i);
        }
        return values;
    }

    protected Array readVariable(final String variableName) {
        try {
            return findVariable(variableName).read();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static float fillValueOf(final Variable variable) {
        final Attribute attribute = variable.findAttribute(FILL_VALUE_ATTRIBUTE);
        if (attribute == null) {
            throw new IllegalStateException(
                "No %s attribute on variable %s".formatted(FILL_VALUE_ATTRIBUTE, variable.getFullName())
            );
        }
        return attribute.getNumericValue().floatValue();
    }

    private Dimension findDimension(final String name) {
        final Dimension dimension = netcdfFile.findDimension(name);
        if (dimension == null) {
            throw new IllegalStateException(
                "No dimension named %s in %s".formatted(name, netcdfFile.getLocation())
            );
        }
        return dimension;
    }

    protected Variable findVariable(final String name) {
        final Variable variable = netcdfFile.findVariable(name);
        if (variable == null) {
            throw new IllegalStateException(
                "No variable named %s in %s".formatted(name, netcdfFile.getLocation())
            );
        }
        return variable;
    }

    @Override
    public void close() {
        try {
            netcdfFile.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
