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
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.write.NetcdfFormatWriter;
import uk.ac.ox.poseidon.core.Scope;

class StaticNetCdfGridReaderFactoryTest {

  private static final float FILL_VALUE = -1f;

  @TempDir
  private Path tempDir;

  @Test
  void resolvingTheFactoryDoesNoFileIO() {
    final StaticNetCdfGridReaderFactory<Scope> factory =
      new StaticNetCdfGridReaderFactory<>(
        scope -> tempDir.resolve("does-not-exist.nc"),
        "latitude",
        "longitude"
      );

    assertThatCode(() ->
      factory.get(Scope.GLOBAL_SCOPE)
    ).doesNotThrowAnyException();
  }

  @Test
  void supplierGetProducesTwoIndependentUsableReaders() throws IOException {
    final Path fixture = writeFixture();

    final StaticNetCdfGridReaderFactory<Scope> factory =
      new StaticNetCdfGridReaderFactory<>(
        scope -> fixture,
        "latitude",
        "longitude"
      );
    final Supplier<StaticNetCdfGridReader> supplier = factory.get(
      Scope.GLOBAL_SCOPE
    );

    try (
      final StaticNetCdfGridReader first = supplier.get();
      final StaticNetCdfGridReader second = supplier.get()
    ) {
      assertThat(first).isNotSameAs(second);
      assertThat(first.getDataVariableNames()).containsExactly("mpa_4");
      assertThat(second.getDataVariableNames()).containsExactly("mpa_4");
    }
  }

  private Path writeFixture() throws IOException {
    final Path file = tempDir.resolve("fixture.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(file.toString());
    final Dimension latDim = writerBuilder.addDimension("latitude", 1);
    final Dimension lonDim = writerBuilder.addDimension("longitude", 1);
    writerBuilder.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
    writerBuilder
      .addVariable("mpa_4", DataType.FLOAT, List.of(latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    try (NetcdfFormatWriter writer = writerBuilder.build()) {
      writer.write("latitude", Array.makeFromJavaArray(new double[] { 1.0 }));
      writer.write("longitude", Array.makeFromJavaArray(new double[] { 2.0 }));
      writer.write("mpa_4", Array.makeFromJavaArray(new float[][] { { 1f } }));
    } catch (final ucar.ma2.InvalidRangeException e) {
      throw new RuntimeException(e);
    }
    return file;
  }
}
