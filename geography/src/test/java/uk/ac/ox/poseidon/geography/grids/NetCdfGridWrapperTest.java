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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetCdfGridWrapperTest {

    private static final float FILL_VALUE = -999f;

    @TempDir
    private Path tempDir;

    private Path fixture;

    @BeforeEach
    void setUp() throws IOException {
        fixture = tempDir.resolve("fixture.nc");
        try (NetcdfFileWriter writer =
                 NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fixture.toString())) {

            final Dimension timeDim = writer.addDimension("time", 2);
            final Dimension latDim = writer.addDimension("latitude", 2);
            final Dimension lonDim = writer.addDimension("longitude", 3);

            final Variable timeVar = writer.addVariable("time", DataType.DOUBLE, List.of(timeDim));
            writer.addVariableAttribute(timeVar, new Attribute("units", "days since 1970-01-01"));
            final Variable latVar = writer.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
            final Variable lonVar = writer.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
            final Variable crsVar = writer.addVariable("crs", DataType.INT, List.of());
            final Variable tempVar =
                writer.addVariable("TEMP", DataType.FLOAT, List.of(timeDim, latDim, lonDim));
            writer.addVariableAttribute(tempVar, new Attribute("_FillValue", FILL_VALUE));

            writer.create();

            writer.write(timeVar, Array.makeFromJavaArray(new double[]{0, 1}));
            writer.write(latVar, Array.makeFromJavaArray(new double[]{5.0, 4.0}));
            writer.write(lonVar, Array.makeFromJavaArray(new double[]{10.0, 20.0, 30.0}));
            writer.write(crsVar, Array.factory(DataType.INT, new int[0], new int[]{0}));
            writer.write(
                tempVar,
                Array.makeFromJavaArray(new float[][][]{
                    {{1f, 2f, FILL_VALUE}, {4f, 5f, 6f}},
                    {{7f, 8f, 9f}, {10f, 11f, 12f}}
                })
            );
        } catch (final ucar.ma2.InvalidRangeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void dataVariableNamesExcludeCoordinateAndCrsVariables() {
        try (NetCdfGridWrapper wrapper = new NetCdfGridWrapper(fixture)) {
            assertThat(wrapper.getDataVariableNames()).containsExactly("TEMP");
        }
    }

    @Test
    void dimensionSizes() {
        try (NetCdfGridWrapper wrapper = new NetCdfGridWrapper(fixture)) {
            assertThat(wrapper.getLonDimensionSize()).isEqualTo(3);
            assertThat(wrapper.getLatDimensionSize()).isEqualTo(2);
        }
    }

    @Test
    void coordinateValues() {
        try (NetCdfGridWrapper wrapper = new NetCdfGridWrapper(fixture)) {
            assertThat(wrapper.getLongitudes()).containsExactly(10.0, 20.0, 30.0);
            assertThat(wrapper.getLatitudes()).containsExactly(5.0, 4.0);
        }
    }

    @Test
    void epochDaysRoundTrip() {
        try (NetCdfGridWrapper wrapper = new NetCdfGridWrapper(fixture)) {
            assertThat(wrapper.getEpochDays()).containsExactly(0L, 1L);
        }
    }

    @Test
    void readSliceIsReorientedToLonByLatWithFillConvertedToNaN() {
        try (NetCdfGridWrapper wrapper = new NetCdfGridWrapper(fixture)) {
            final double[][] slice = wrapper.readSlice("TEMP", 0);

            // written as [lat][lon] = {{1,2,FILL},{4,5,6}}; expect [lon][lat]
            assertThat(slice[0][0]).isEqualTo(1.0);
            assertThat(slice[1][0]).isEqualTo(2.0);
            assertThat(slice[2][0]).isNaN();
            assertThat(slice[0][1]).isEqualTo(4.0);
            assertThat(slice[1][1]).isEqualTo(5.0);
            assertThat(slice[2][1]).isEqualTo(6.0);

            final double[][] secondSlice = wrapper.readSlice("TEMP", 1);
            assertThat(secondSlice[0][0]).isEqualTo(7.0);
            assertThat(secondSlice[2][1]).isEqualTo(12.0);
        }
    }

}
