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

package uk.ac.ox.poseidon.biology.biomass;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.biology.biomass.Factories.timeIndexedBiomassGridsFromNetCdf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.write.NetcdfFormatWriter;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class FactoriesTest {

  private static final float FILL_VALUE = -999f;
  private static final Species HKE = new Species("HKE", null, null);
  private final ModelGrid modelGrid = ModelGrid.create(
    3,
    2,
    new Envelope(5, 35, 3.5, 5.5)
  );

  @TempDir
  private Path tempDir;

  @Test
  void timeIndexedBiomassGridsFromNetCdfDefaultsSeparatorAndDimensionNames()
    throws IOException {
    final Path fixture = writeFixture();

    final ImmutableMap<
      LocalDateTime,
      ImmutableList<ImmutableBiomassGrid>
    > result = timeIndexedBiomassGridsFromNetCdf(
      scope -> modelGrid,
      scope -> List.of(HKE),
      scope -> fixture
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(result.keySet()).containsExactly(
      LocalDateTime.of(2020, 1, 1, 0, 0)
    );
    assertThat(result.get(LocalDateTime.of(2020, 1, 1, 0, 0)))
      .extracting(ImmutableBiomassGrid::getSpecies)
      .containsExactly(HKE);
  }

  private Path writeFixture() throws IOException {
    final Path file = tempDir.resolve("fixture.nc");
    final NetcdfFormatWriter.Builder writerBuilder =
      NetcdfFormatWriter.createNewNetcdf3(file.toString());
    final Dimension timeDim = writerBuilder.addDimension("time", 1);
    final Dimension latDim = writerBuilder.addDimension("latitude", 2);
    final Dimension lonDim = writerBuilder.addDimension("longitude", 3);
    writerBuilder
      .addVariable("time", DataType.DOUBLE, List.of(timeDim))
      .addAttribute(new Attribute("units", "days since 1970-01-01"));
    writerBuilder.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
    writerBuilder.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
    writerBuilder
      .addVariable("HKE", DataType.FLOAT, List.of(timeDim, latDim, lonDim))
      .addAttribute(new Attribute("_FillValue", FILL_VALUE));
    try (NetcdfFormatWriter writer = writerBuilder.build()) {
      writer.write(
        "time",
        Array.makeFromJavaArray(new double[] {
          LocalDateTime.of(2020, 1, 1, 0, 0).toLocalDate().toEpochDay()
        })
      );
      writer.write(
        "latitude",
        Array.makeFromJavaArray(new double[] { 5.0, 4.0 })
      );
      writer.write(
        "longitude",
        Array.makeFromJavaArray(new double[] { 10.0, 20.0, 30.0 })
      );
      writer.write(
        "HKE",
        Array.makeFromJavaArray(new float[][][] {
          { { 1f, 2f, 3f }, { 4f, 5f, 6f } }
        })
      );
    } catch (final InvalidRangeException e) {
      throw new RuntimeException(e);
    }
    return file;
  }
}
