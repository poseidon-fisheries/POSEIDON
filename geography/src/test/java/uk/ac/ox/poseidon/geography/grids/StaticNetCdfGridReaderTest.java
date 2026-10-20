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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.write.NetcdfFormatWriter;

class StaticNetCdfGridReaderTest {

  private static final float FILL_VALUE = -1f;

  @TempDir
  private Path tempDir;

  private Path fixture;

  @BeforeEach
  void setUp() throws IOException {
    fixture = tempDir.resolve("fixture.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(fixture.toString());
    final Dimension latDim = writerBuilder.addDimension("latitude", 2);
    final Dimension lonDim = writerBuilder.addDimension("longitude", 3);

    writerBuilder.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
    writerBuilder.addVariable("crs", DataType.INT, List.of());
    writerBuilder
      .addVariable("mpa_4", DataType.FLOAT, List.of(latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    writerBuilder
      .addVariable("mpa_5", DataType.FLOAT, List.of(latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));

    try (NetcdfFormatWriter writer = writerBuilder.build()) {
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
        "mpa_4",
        Array.makeFromJavaArray(new float[][] {
          { 0f, 1f, FILL_VALUE },
          { 1f, 0f, 0f }
        })
      );
      writer.write(
        "mpa_5",
        Array.makeFromJavaArray(new float[][] {
          { 1f, 1f, 1f },
          { 0f, 0f, 0f }
        })
      );
    } catch (final ucar.ma2.InvalidRangeException e) {
      throw new RuntimeException(e);
    }
  }

  private StaticNetCdfGridReader openReader() {
    return new StaticNetCdfGridReader(fixture, "latitude", "longitude");
  }

  @Test
  void dataVariableNamesExcludeCoordinateAndCrsVariables() {
    try (StaticNetCdfGridReader reader = openReader()) {
      assertThat(reader.getDataVariableNames()).containsExactlyInAnyOrder(
        "mpa_4",
        "mpa_5"
      );
    }
  }

  @Test
  void readGridIsReorientedToLonByLatWithFillConvertedToNaN() {
    try (StaticNetCdfGridReader reader = openReader()) {
      final double[][] grid = reader.readGrid("mpa_4");

      // written as [lat][lon] = {{0,1,FILL},{1,0,0}}; expect [lon][lat]
      assertThat(grid[0][0]).isEqualTo(0.0);
      assertThat(grid[1][0]).isEqualTo(1.0);
      assertThat(grid[2][0]).isNaN();
      assertThat(grid[0][1]).isEqualTo(1.0);
      assertThat(grid[1][1]).isEqualTo(0.0);
      assertThat(grid[2][1]).isEqualTo(0.0);

      final double[][] otherGrid = reader.readGrid("mpa_5");
      assertThat(otherGrid[0][0]).isEqualTo(1.0);
      assertThat(otherGrid[0][1]).isEqualTo(0.0);
    }
  }

  @Test
  void dimensionNamesAreConfigurable() throws IOException {
    final Path renamedFixture = tempDir.resolve("renamed.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(renamedFixture.toString());
    final Dimension latDim = writerBuilder.addDimension("y", 1);
    final Dimension lonDim = writerBuilder.addDimension("x", 1);
    writerBuilder.addVariable("y", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("x", DataType.DOUBLE, List.of(lonDim));
    writerBuilder
      .addVariable("VAR", DataType.FLOAT, List.of(latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    try (NetcdfFormatWriter writer = writerBuilder.build()) {
      writer.write("y", Array.makeFromJavaArray(new double[] { 1.0 }));
      writer.write("x", Array.makeFromJavaArray(new double[] { 2.0 }));
      writer.write("VAR", Array.makeFromJavaArray(new float[][] { { 42f } }));
    } catch (final ucar.ma2.InvalidRangeException e) {
      throw new RuntimeException(e);
    }

    try (
      StaticNetCdfGridReader reader = new StaticNetCdfGridReader(
        renamedFixture,
        "y",
        "x"
      )
    ) {
      assertThat(reader.getDataVariableNames()).containsExactly("VAR");
      assertThat(reader.readGrid("VAR")[0][0]).isEqualTo(42.0);
    }
  }

  @Test
  void timeLatLonVariableIsNotConsideredAStaticGrid() throws IOException {
    final Path fileWithTimeVariable = tempDir.resolve("with-time.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(fileWithTimeVariable.toString());
    final Dimension timeDim = writerBuilder.addDimension("time", 1);
    final Dimension latDim = writerBuilder.addDimension("latitude", 1);
    final Dimension lonDim = writerBuilder.addDimension("longitude", 1);
    writerBuilder.addVariable("time", DataType.DOUBLE, List.of(timeDim));
    writerBuilder.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
    writerBuilder
      .addVariable("TIMEVAR", DataType.FLOAT, List.of(timeDim, latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    writerBuilder.build().close();

    try (
      StaticNetCdfGridReader reader = new StaticNetCdfGridReader(
        fileWithTimeVariable,
        "latitude",
        "longitude"
      )
    ) {
      assertThat(reader.getDataVariableNames()).isEmpty();
    }
  }
}
