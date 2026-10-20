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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.write.NetcdfFormatWriter;

class TimeIndexedNetCdfGridReaderTest {

  private static final float FILL_VALUE = -999f;

  @TempDir
  private Path tempDir;

  private Path fixture;

  @BeforeEach
  void setUp() throws IOException {
    fixture = tempDir.resolve("fixture.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(fixture.toString());
    final Dimension timeDim = writerBuilder.addDimension("time", 2);
    final Dimension latDim = writerBuilder.addDimension("latitude", 2);
    final Dimension lonDim = writerBuilder.addDimension("longitude", 3);

    writerBuilder
      .addVariable("time", DataType.DOUBLE, List.of(timeDim))
      .addAttribute(new Attribute("units", "days since 1970-01-01"));
    writerBuilder.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
    writerBuilder.addVariable("crs", DataType.INT, List.of());
    writerBuilder
      .addVariable("TEMP", DataType.FLOAT, List.of(timeDim, latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));

    try (NetcdfFormatWriter writer = writerBuilder.build()) {
      writer.write("time", Array.makeFromJavaArray(new double[] { 0, 1 }));
      writer.write(
        "latitude",
        Array.makeFromJavaArray(new double[] { 5.0, 4.0 })
      );
      writer.write(
        "longitude",
        Array.makeFromJavaArray(new double[] { 10.0, 20.0, 30.0 })
      );
      writer.write(
        "crs",
        Array.factory(DataType.INT, new int[0], new int[] { 0 })
      );
      writer.write(
        "TEMP",
        Array.makeFromJavaArray(new float[][][] {
          { { 1f, 2f, FILL_VALUE }, { 4f, 5f, 6f } },
          { { 7f, 8f, 9f }, { 10f, 11f, 12f } }
        })
      );
    } catch (final ucar.ma2.InvalidRangeException e) {
      throw new RuntimeException(e);
    }
  }

  private TimeIndexedNetCdfGridReader openReader() {
    return new TimeIndexedNetCdfGridReader(
      fixture,
      "time",
      "latitude",
      "longitude"
    );
  }

  @Test
  void dataVariableNamesExcludeCoordinateAndCrsVariables() {
    try (TimeIndexedNetCdfGridReader reader = openReader()) {
      assertThat(reader.getDataVariableNames()).containsExactly("TEMP");
    }
  }

  @Test
  void dimensionSizes() {
    try (TimeIndexedNetCdfGridReader reader = openReader()) {
      assertThat(reader.getLonDimensionSize()).isEqualTo(3);
      assertThat(reader.getLatDimensionSize()).isEqualTo(2);
    }
  }

  @Test
  void coordinateValues() {
    try (TimeIndexedNetCdfGridReader reader = openReader()) {
      assertThat(reader.getLongitudes()).containsExactly(10.0, 20.0, 30.0);
      assertThat(reader.getLatitudes()).containsExactly(5.0, 4.0);
    }
  }

  @Test
  void dateTimesRoundTrip() {
    try (TimeIndexedNetCdfGridReader reader = openReader()) {
      assertThat(reader.getDateTimes()).containsExactly(
        LocalDateTime.of(1970, 1, 1, 0, 0),
        LocalDateTime.of(1970, 1, 2, 0, 0)
      );
    }
  }

  @Test
  void fractionalDayValuesPreserveTimeOfDay() throws IOException {
    final Path fractionalFixture = tempDir.resolve("fractional.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(fractionalFixture.toString());
    final Dimension timeDim = writerBuilder.addDimension("time", 1);
    final Dimension latDim = writerBuilder.addDimension("latitude", 1);
    final Dimension lonDim = writerBuilder.addDimension("longitude", 1);
    writerBuilder
      .addVariable("time", DataType.DOUBLE, List.of(timeDim))
      .addAttribute(new Attribute("units", "days since 1970-01-01"));
    writerBuilder.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
    writerBuilder
      .addVariable("TEMP", DataType.FLOAT, List.of(timeDim, latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    try (NetcdfFormatWriter writer = writerBuilder.build()) {
      // 18262.5 days since 1970-01-01 = noon on 2020-01-01, not midnight.
      writer.write("time", Array.makeFromJavaArray(new double[] { 18262.5 }));
      writer.write("latitude", Array.makeFromJavaArray(new double[] { 1.0 }));
      writer.write("longitude", Array.makeFromJavaArray(new double[] { 2.0 }));
      writer.write(
        "TEMP",
        Array.makeFromJavaArray(new float[][][] { { { 1f } } })
      );
    } catch (final ucar.ma2.InvalidRangeException e) {
      throw new RuntimeException(e);
    }

    try (
      TimeIndexedNetCdfGridReader reader = new TimeIndexedNetCdfGridReader(
        fractionalFixture,
        "time",
        "latitude",
        "longitude"
      )
    ) {
      assertThat(reader.getDateTimes()).containsExactly(
        LocalDateTime.of(2020, 1, 1, 12, 0)
      );
    }
  }

  @Test
  void readSliceIsReorientedToLonByLatWithFillConvertedToNaN() {
    try (TimeIndexedNetCdfGridReader reader = openReader()) {
      final double[][] slice = reader.readSlice("TEMP", 0);

      // written as [lat][lon] = {{1,2,FILL},{4,5,6}}; expect [lon][lat]
      assertThat(slice[0][0]).isEqualTo(1.0);
      assertThat(slice[1][0]).isEqualTo(2.0);
      assertThat(slice[2][0]).isNaN();
      assertThat(slice[0][1]).isEqualTo(4.0);
      assertThat(slice[1][1]).isEqualTo(5.0);
      assertThat(slice[2][1]).isEqualTo(6.0);

      final double[][] secondSlice = reader.readSlice("TEMP", 1);
      assertThat(secondSlice[0][0]).isEqualTo(7.0);
      assertThat(secondSlice[2][1]).isEqualTo(12.0);
    }
  }

  @Test
  void dimensionNamesAreConfigurable() throws IOException {
    final Path renamedFixture = tempDir.resolve("renamed.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(renamedFixture.toString());
    final Dimension timeDim = writerBuilder.addDimension("t", 1);
    final Dimension latDim = writerBuilder.addDimension("y", 1);
    final Dimension lonDim = writerBuilder.addDimension("x", 1);
    writerBuilder.addVariable("t", DataType.DOUBLE, List.of(timeDim));
    writerBuilder.addVariable("y", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("x", DataType.DOUBLE, List.of(lonDim));
    writerBuilder
      .addVariable("VAR", DataType.FLOAT, List.of(timeDim, latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    try (NetcdfFormatWriter writer = writerBuilder.build()) {
      writer.write("t", Array.makeFromJavaArray(new double[] { 0 }));
      writer.write("y", Array.makeFromJavaArray(new double[] { 1.0 }));
      writer.write("x", Array.makeFromJavaArray(new double[] { 2.0 }));
      writer.write(
        "VAR",
        Array.makeFromJavaArray(new float[][][] { { { 42f } } })
      );
    } catch (final ucar.ma2.InvalidRangeException e) {
      throw new RuntimeException(e);
    }

    try (
      TimeIndexedNetCdfGridReader reader = new TimeIndexedNetCdfGridReader(
        renamedFixture,
        "t",
        "y",
        "x"
      )
    ) {
      assertThat(reader.getDataVariableNames()).containsExactly("VAR");
      assertThat(reader.readSlice("VAR", 0)[0][0]).isEqualTo(42.0);
    }
  }
}
