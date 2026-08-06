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
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Reads a NetCDF file holding a stack of {@code (time, latitude, longitude)} rasters, one per
 * data variable.
 */
public final class TimeIndexedNetCdfGridReader extends AbstractNetCdfGridReader {

    private static final LocalDateTime EPOCH_DATE_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final long SECONDS_PER_DAY = 86_400L;

    private final String timeDimensionName;

    public TimeIndexedNetCdfGridReader(
        final Path ncFile,
        final String timeDimensionName,
        final String latitudeDimensionName,
        final String longitudeDimensionName
    ) {
        super(ncFile, latitudeDimensionName, longitudeDimensionName);
        this.timeDimensionName = timeDimensionName;
    }

    /**
     * Names of the data variables shaped exactly {@code (time, latitude, longitude)}, excluding
     * the coordinate variables and any grid-mapping variable (e.g. {@code crs}).
     */
    public List<String> getDataVariableNames() {
        return netcdfFile
            .getVariables()
            .stream()
            .filter(this::isTimeLatitudeLongitudeVariable)
            .map(Variable::getFullName)
            .toList();
    }

    private boolean isTimeLatitudeLongitudeVariable(final Variable variable) {
        final List<Dimension> dimensions = variable.getDimensions();
        return dimensions.size() == 3
            && timeDimensionName.equals(dimensions.get(0).getShortName())
            && latitudeDimensionName.equals(dimensions.get(1).getShortName())
            && longitudeDimensionName.equals(dimensions.get(2).getShortName());
    }

    /**
     * {@code time} variable values (CF "days since 1970-01-01", not necessarily whole days), in
     * file order, converted to the nearest second — {@link uk.ac.ox.poseidon.core.schedule.TemporalSchedule}
     * itself only resolves elapsed time to whole seconds, so rounding here loses nothing usable
     * downstream while still preserving any sub-day precision the source data actually carries.
     */
    public List<LocalDateTime> getDateTimes() {
        final Array array = readVariable(timeDimensionName);
        return IntStream
            .range(0, (int) array.getSize())
            .mapToObj(i -> EPOCH_DATE_TIME.plusSeconds(Math.round(array.getDouble(i) * SECONDS_PER_DAY)))
            .toList();
    }

    /**
     * Reads one {@code (latitude, longitude)} slice of the named variable at the given time
     * index, reoriented to {@code [longitudeIndex][latitudeIndex]}, with {@code _FillValue}
     * cells converted to {@link Double#NaN}.
     */
    public double[][] readSlice(final String variableName, final int timeIndex) {
        final Variable variable = findVariable(variableName);
        final int latDim = getLatDimensionSize();
        final int lonDim = getLonDimensionSize();
        return readAndReorient(variable, new int[]{timeIndex, 0, 0}, new int[]{1, latDim, lonDim});
    }

}
