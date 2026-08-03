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

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.LongStream;

/**
 * Reads a NetCDF file on a regular (time, latitude, longitude) grid, exposing only POSEIDON/JDK
 * types (no {@code ucar.*} types leak out of this class). Reads via the raw, non-"enhanced" CDM
 * API so that {@code _FillValue} substitution is the only conversion applied — CDM's automatic
 * enhancement would also silently apply {@code scale_factor}/{@code add_offset}/unit conversions
 * that aren't wanted here.
 */
public final class NetCdfGridWrapper implements AutoCloseable {

    private static final String TIME_DIMENSION = "time";
    private static final String LATITUDE_DIMENSION = "latitude";
    private static final String LONGITUDE_DIMENSION = "longitude";
    private static final String FILL_VALUE_ATTRIBUTE = "_FillValue";

    private final NetcdfFile netcdfFile;

    public NetCdfGridWrapper(final Path ncFile) {
        try {
            this.netcdfFile = NetcdfFiles.open(ncFile.toString());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Names of the data variables shaped exactly {@code (time, latitude, longitude)}, excluding
     * the coordinate variables and any grid-mapping variable (e.g. {@code crs}).
     */
    public List<String> getDataVariableNames() {
        return netcdfFile
            .getVariables()
            .stream()
            .filter(NetCdfGridWrapper::isTimeLatitudeLongitudeVariable)
            .map(Variable::getFullName)
            .toList();
    }

    private static boolean isTimeLatitudeLongitudeVariable(final Variable variable) {
        final List<Dimension> dimensions = variable.getDimensions();
        return dimensions.size() == 3
            && TIME_DIMENSION.equals(dimensions.get(0).getShortName())
            && LATITUDE_DIMENSION.equals(dimensions.get(1).getShortName())
            && LONGITUDE_DIMENSION.equals(dimensions.get(2).getShortName());
    }

    /**
     * Raw {@code time} variable values (CF "days since 1970-01-01"), in file order.
     */
    public List<Long> getEpochDays() {
        final Array array = readVariable(TIME_DIMENSION);
        return LongStream
            .range(0, array.getSize())
            .mapToObj(i -> (long) array.getDouble((int) i))
            .toList();
    }

    public int getLonDimensionSize() {
        return findDimension(LONGITUDE_DIMENSION).getLength();
    }

    public int getLatDimensionSize() {
        return findDimension(LATITUDE_DIMENSION).getLength();
    }

    /**
     * Longitude coordinate values (ascending, degrees_east), one per longitude cell.
     */
    public double[] getLongitudes() {
        return readDoubles(LONGITUDE_DIMENSION);
    }

    /**
     * Latitude coordinate values (descending, degrees_north), one per latitude cell.
     */
    public double[] getLatitudes() {
        return readDoubles(LATITUDE_DIMENSION);
    }

    private double[] readDoubles(final String variableName) {
        final Array array = readVariable(variableName);
        final double[] values = new double[(int) array.getSize()];
        for (int i = 0; i < values.length; i++) {
            values[i] = array.getDouble(i);
        }
        return values;
    }

    private Array readVariable(final String variableName) {
        try {
            return findVariable(variableName).read();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads one (latitude, longitude) slice of the named variable at the given time index,
     * reoriented to {@code [longitudeIndex][latitudeIndex]} (i.e. {@code ModelGrid}'s
     * {@code [x][y]} convention), with {@code _FillValue} cells converted to {@link Double#NaN}.
     */
    public double[][] readSlice(final String variableName, final int timeIndex) {
        final Variable variable = findVariable(variableName);
        final int latDim = getLatDimensionSize();
        final int lonDim = getLonDimensionSize();
        final float fillValue = fillValueOf(variable);

        final Array slice;
        try {
            slice = variable.read(new int[]{timeIndex, 0, 0}, new int[]{1, latDim, lonDim});
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

    private Variable findVariable(final String name) {
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
