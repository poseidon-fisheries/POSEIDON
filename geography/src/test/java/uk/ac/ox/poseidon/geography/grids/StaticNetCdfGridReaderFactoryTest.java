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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class StaticNetCdfGridReaderFactoryTest {

    private static final float FILL_VALUE = -1f;

    @TempDir
    private Path tempDir;

    @Test
    void resolvingTheFactoryDoesNoFileIO() {
        final StaticNetCdfGridReaderFactory<Scope> factory = new StaticNetCdfGridReaderFactory<>(
            scope -> tempDir.resolve("does-not-exist.nc"),
            "latitude", "longitude"
        );

        assertThatCode(() -> factory.get(mock(Scope.class))).doesNotThrowAnyException();
    }

    @Test
    void supplierGetProducesTwoIndependentUsableReaders() throws IOException {
        final Path fixture = writeFixture();

        final StaticNetCdfGridReaderFactory<Scope> factory = new StaticNetCdfGridReaderFactory<>(
            scope -> fixture,
            "latitude", "longitude"
        );
        final Supplier<StaticNetCdfGridReader> supplier = factory.get(mock(Scope.class));

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
        try (NetcdfFileWriter writer =
                 NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, file.toString())) {
            final Dimension latDim = writer.addDimension("latitude", 1);
            final Dimension lonDim = writer.addDimension("longitude", 1);
            final Variable latVar = writer.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
            final Variable lonVar = writer.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
            final Variable dataVar =
                writer.addVariable("mpa_4", DataType.FLOAT, List.of(latDim, lonDim));
            writer.addVariableAttribute(dataVar, new Attribute("_FillValue", FILL_VALUE));
            writer.create();
            writer.write(latVar, Array.makeFromJavaArray(new double[]{1.0}));
            writer.write(lonVar, Array.makeFromJavaArray(new double[]{2.0}));
            writer.write(dataVar, Array.makeFromJavaArray(new float[][]{{1f}}));
        } catch (final ucar.ma2.InvalidRangeException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

}
