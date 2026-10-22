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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.write.NetcdfFormatWriter;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Envelope;

class StaticGridsFromNetCdfFactoryTest {

  private static final float FILL_VALUE = -1f;
  private static final double[] LONGITUDES = { 10.0, 20.0, 30.0 };
  private static final double[] LATITUDES = { 5.0, 4.0 };

  @TempDir
  private Path tempDir;

  // Aligned so that modelGrid.toCoordinate(Cell(x, y)) exactly equals
  // (LONGITUDES[x], LATITUDES[y]) for this 3x2 grid.
  private final ModelGrid modelGrid = ModelGrid.create(
    3,
    2,
    new Envelope(5, 35, 3.5, 5.5)
  );

  @Test
  void happyPath() throws IOException {
    final Path fixture = writeFixture(LONGITUDES, LATITUDES);

    final StaticGridsFromNetCdfFactory<Scope> factory =
      new StaticGridsFromNetCdfFactory<>(
        scope -> modelGrid,
        new StaticNetCdfGridReaderFactory<>(
          scope -> fixture,
          "latitude",
          "longitude"
        )
      );

    final ImmutableMap<String, DoubleGridWrapper> result = factory.get(
      Scope.GLOBAL_SCOPE
    );

    assertThat(result).isInstanceOf(ImmutableMap.class);
    assertThat(result.keySet()).containsExactlyInAnyOrder("mpa_4", "mpa_5");

    // written as [lat][lon] = {{0,1,FILL},{1,0,0}}; expect [lon][lat] via
    // Cell(x=lon, y=lat)
    assertThat(result.get("mpa_4").getValue(new Cell(0, 0))).isEqualTo(0.0);
    assertThat(result.get("mpa_4").getValue(new Cell(1, 0))).isEqualTo(1.0);
    assertThat(result.get("mpa_4").getValue(new Cell(2, 0))).isNaN();
    assertThat(result.get("mpa_5").getValue(new Cell(0, 0))).isEqualTo(1.0);
    assertThat(result.get("mpa_5").getValue(new Cell(0, 1))).isEqualTo(0.0);
  }

  @Test
  void misalignedGridThrows() throws IOException {
    final double[] shiftedLongitudes = { 11.0, 21.0, 31.0 };
    final Path fixture = writeFixture(shiftedLongitudes, LATITUDES);

    final StaticGridsFromNetCdfFactory<Scope> factory =
      new StaticGridsFromNetCdfFactory<>(
        scope -> modelGrid,
        new StaticNetCdfGridReaderFactory<>(
          scope -> fixture,
          "latitude",
          "longitude"
        )
      );

    assertThatThrownBy(() -> factory.get(Scope.GLOBAL_SCOPE))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("does not align");
  }

  private Path writeFixture(final double[] longitudes, final double[] latitudes)
    throws IOException {
    final Path file = tempDir.resolve("fixture.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(file.toString());
    final Dimension latDim = writerBuilder.addDimension(
      "latitude",
      latitudes.length
    );
    final Dimension lonDim = writerBuilder.addDimension(
      "longitude",
      longitudes.length
    );

    writerBuilder.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
    writerBuilder
      .addVariable("mpa_4", DataType.FLOAT, List.of(latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    writerBuilder
      .addVariable("mpa_5", DataType.FLOAT, List.of(latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));

    try (NetcdfFormatWriter writer = writerBuilder.build()) {
      writer.write("latitude", Array.makeFromJavaArray(latitudes));
      writer.write("longitude", Array.makeFromJavaArray(longitudes));
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
    } catch (final InvalidRangeException e) {
      throw new RuntimeException(e);
    }
    return file;
  }
}
