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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sim.util.Int2D;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.TimeIndexedNetCdfGridReaderFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TimeIndexedBiomassGridsFromNetCdfFactoryTest {

    private static final float FILL_VALUE = -999f;
    private static final double[] LONGITUDES = {10.0, 20.0, 30.0};
    private static final double[] LATITUDES = {5.0, 4.0};

    private static final Species ANE_ADULT = new Species("ANE", "adult", null);
    private static final Species HKE = new Species("HKE", null, null);

    @TempDir
    private Path tempDir;

    // Aligned so that modelGrid.toCoordinate(Int2D(x, y)) exactly equals
    // (LONGITUDES[x], LATITUDES[y]) for this 3x2 grid.
    private final ModelGrid modelGrid = ModelGrid.create(3, 2, new Envelope(5, 35, 3.5, 5.5));

    @Test
    void happyPath() throws IOException {
        final LocalDate date0 = LocalDate.of(2020, 1, 1);
        final LocalDate date1 = LocalDate.of(2020, 6, 15);
        final LocalDate date2 = LocalDate.of(2021, 1, 1);
        final Path fixture = writeFixture(
            LONGITUDES, LATITUDES,
            List.of(date0, date1, date2),
            List.of("ANE_adult", "HKE"),
            true
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(ANE_ADULT, HKE),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        final ImmutableMap<LocalDateTime, ImmutableList<ImmutableBiomassGrid>> result =
            factory.get(mock(Scope.class));

        assertThat(result).isInstanceOf(ImmutableMap.class);
        assertThat(result.keySet())
            .containsExactly(date0.atStartOfDay(), date1.atStartOfDay(), date2.atStartOfDay());

        final ImmutableList<ImmutableBiomassGrid> grids0 = result.get(date0.atStartOfDay());
        assertThat(grids0).isInstanceOf(ImmutableList.class);
        assertThat(grids0)
            .extracting(ImmutableBiomassGrid::getSpecies)
            .containsExactly(ANE_ADULT, HKE);

        // The fixture writes a deliberate fill cell for the first variable (ANE_adult)
        // at time index 0, latitude index 0, longitude index 2.
        assertThat(grids0.get(0).getValue(new Int2D(2, 0))).isNaN();
        assertThat(grids0.get(0).getValue(new Int2D(0, 0)))
            .isEqualTo(expectedValue(0, 0, 0, 0));
        assertThat(grids0.get(1).getValue(new Int2D(1, 1)))
            .isEqualTo(expectedValue(1, 0, 1, 1));
    }

    @Test
    void duplicateDateThrows() throws IOException {
        final LocalDate sameDate = LocalDate.of(2020, 1, 1);
        final Path fixture = writeFixture(
            LONGITUDES, LATITUDES,
            List.of(sameDate, sameDate),
            List.of("HKE"),
            false
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(HKE),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        assertThatThrownBy(() -> factory.get(mock(Scope.class)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Duplicate date");
    }

    @Test
    void unmatchedVariableThrows() throws IOException {
        final Path fixture = writeFixture(
            LONGITUDES, LATITUDES,
            List.of(LocalDate.of(2020, 1, 1)),
            List.of("UNKNOWN_CODE"),
            false
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(HKE),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        assertThatThrownBy(() -> factory.get(mock(Scope.class)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("UNKNOWN_CODE");
    }

    @Test
    void unmatchedSpeciesThrows() throws IOException {
        final Path fixture = writeFixture(
            LONGITUDES, LATITUDES,
            List.of(LocalDate.of(2020, 1, 1)),
            List.of("HKE"),
            false
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(HKE, ANE_ADULT),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        assertThatThrownBy(() -> factory.get(mock(Scope.class)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ANE");
    }

    @Test
    void misalignedGridThrows() throws IOException {
        // Same dimension sizes, but longitudes shifted so cell centers no longer
        // line up with the ModelGrid's — must be caught even though the shape matches.
        final double[] shiftedLongitudes = {11.0, 21.0, 31.0};
        final Path fixture = writeFixture(
            shiftedLongitudes, LATITUDES,
            List.of(LocalDate.of(2020, 1, 1)),
            List.of("HKE"),
            false
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(HKE),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        assertThatThrownBy(() -> factory.get(mock(Scope.class)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not align");
    }

    @Test
    void matchesOnAnyConfiguredLifeStageVocabularyAndSeparator() throws IOException {
        // Neither "-" nor "larva"/"spawner" are hardcoded anywhere; both come entirely from the
        // configured species list and the separator argument.
        final Species codLarva = new Species("COD", "larva", null);
        final Species codSpawner = new Species("COD", "spawner", null);
        final Path fixture = writeFixture(
            LONGITUDES, LATITUDES,
            List.of(LocalDate.of(2020, 1, 1)),
            List.of("COD-larva", "COD-spawner"),
            false
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(codLarva, codSpawner),
                "-",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        final ImmutableList<ImmutableBiomassGrid> grids =
            factory.get(mock(Scope.class)).get(LocalDate.of(2020, 1, 1).atStartOfDay());

        assertThat(grids)
            .extracting(ImmutableBiomassGrid::getSpecies)
            .containsExactly(codLarva, codSpawner);
    }

    @Test
    void splitsUnconditionallyEvenForAnUncommonLifeStageValue() throws IOException {
        // "xxx" isn't tied to any pre-known vocabulary; splitting is purely syntactic, so a
        // configured (HKE, xxx) species matches "HKE_xxx" regardless.
        final Species hkeXxx = new Species("HKE", "xxx", null);
        final Path fixture = writeFixture(
            LONGITUDES, LATITUDES,
            List.of(LocalDate.of(2020, 1, 1)),
            List.of("HKE_xxx"),
            false
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(hkeXxx),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        final ImmutableList<ImmutableBiomassGrid> grids =
            factory.get(mock(Scope.class)).get(LocalDate.of(2020, 1, 1).atStartOfDay());

        assertThat(grids).extracting(ImmutableBiomassGrid::getSpecies).containsExactly(hkeXxx);
    }

    @Test
    void unknownSuffixDoesNotFalselyMatchAnUnrelatedBareSpecies() throws IOException {
        // "HKE_xxx" must be parsed as (HKE, xxx) and fail to match the *bare* HKE species — it
        // must not be silently absorbed into HKE just because "xxx" isn't independently known.
        final Path fixture = writeFixture(
            LONGITUDES, LATITUDES,
            List.of(LocalDate.of(2020, 1, 1)),
            List.of("HKE_xxx"),
            false
        );

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(HKE),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        assertThatThrownBy(() -> factory.get(mock(Scope.class)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("HKE_xxx");
    }

    @Test
    void speciesCodeContainingSeparatorThrowsUpFrontBeforeReadingTheFile() {
        final Species badSpecies = new Species("AA_BB", null, null);

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(badSpecies),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(
                    scope -> tempDir.resolve("does-not-need-to-exist.nc"), "time", "latitude", "longitude"
                )
            );

        assertThatThrownBy(() -> factory.get(mock(Scope.class)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AA_BB");
    }

    @Test
    void fractionalTimeValuePreservesTimeOfDay() throws IOException {
        final Path fixture = tempDir.resolve("fractional.nc");
        try (NetcdfFileWriter writer =
                 NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fixture.toString())) {
            final Dimension timeDim = writer.addDimension("time", 1);
            final Dimension latDim = writer.addDimension("latitude", LATITUDES.length);
            final Dimension lonDim = writer.addDimension("longitude", LONGITUDES.length);
            final Variable timeVar = writer.addVariable("time", DataType.DOUBLE, List.of(timeDim));
            writer.addVariableAttribute(timeVar, new Attribute("units", "days since 1970-01-01"));
            final Variable latVar = writer.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
            final Variable lonVar = writer.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));
            final Variable dataVar =
                writer.addVariable("HKE", DataType.FLOAT, List.of(timeDim, latDim, lonDim));
            writer.addVariableAttribute(dataVar, new Attribute("_FillValue", FILL_VALUE));
            writer.create();
            // 18262.5 days since 1970-01-01 = noon on 2020-01-01, not midnight.
            writer.write(timeVar, Array.makeFromJavaArray(new double[]{18262.5}));
            writer.write(latVar, Array.makeFromJavaArray(LATITUDES));
            writer.write(lonVar, Array.makeFromJavaArray(LONGITUDES));
            final float[][][] data = new float[1][LATITUDES.length][LONGITUDES.length];
            for (int i = 0; i < LATITUDES.length; i++) {
                for (int j = 0; j < LONGITUDES.length; j++) {
                    data[0][i][j] = (float) expectedValue(0, 0, i, j);
                }
            }
            writer.write(dataVar, Array.makeFromJavaArray(data));
        } catch (final InvalidRangeException e) {
            throw new RuntimeException(e);
        }

        final TimeIndexedBiomassGridsFromNetCdfFactory<Scope> factory =
            new TimeIndexedBiomassGridsFromNetCdfFactory<>(
                scope -> modelGrid,
                scope -> List.of(HKE),
                "_",
                new TimeIndexedNetCdfGridReaderFactory<>(scope -> fixture, "time", "latitude", "longitude")
            );

        assertThat(factory.get(mock(Scope.class)).keySet())
            .containsExactly(LocalDateTime.of(2020, 1, 1, 12, 0));
    }

    private static double expectedValue(
        final int variableIndex,
        final int timeIndex,
        final int latIndex,
        final int lonIndex
    ) {
        return 1 + variableIndex * 100 + timeIndex * 10 + latIndex * 3 + lonIndex;
    }

    private Path writeFixture(
        final double[] longitudes,
        final double[] latitudes,
        final List<LocalDate> dates,
        final List<String> variableNames,
        final boolean withFillCell
    ) throws IOException {
        final Path file = tempDir.resolve("fixture-" + System.identityHashCode(dates) + ".nc");
        try (NetcdfFileWriter writer =
                 NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, file.toString())) {

            final Dimension timeDim = writer.addDimension("time", dates.size());
            final Dimension latDim = writer.addDimension("latitude", latitudes.length);
            final Dimension lonDim = writer.addDimension("longitude", longitudes.length);

            final Variable timeVar = writer.addVariable("time", DataType.DOUBLE, List.of(timeDim));
            writer.addVariableAttribute(timeVar, new Attribute("units", "days since 1970-01-01"));
            final Variable latVar = writer.addVariable("latitude", DataType.DOUBLE, List.of(latDim));
            final Variable lonVar = writer.addVariable("longitude", DataType.DOUBLE, List.of(lonDim));

            final List<Variable> dataVars = variableNames.stream()
                .map(name -> {
                    final Variable v =
                        writer.addVariable(name, DataType.FLOAT, List.of(timeDim, latDim, lonDim));
                    writer.addVariableAttribute(v, new Attribute("_FillValue", FILL_VALUE));
                    return v;
                })
                .toList();

            writer.create();

            writer.write(
                timeVar,
                Array.makeFromJavaArray(dates.stream().mapToLong(LocalDate::toEpochDay).toArray())
            );
            writer.write(latVar, Array.makeFromJavaArray(latitudes));
            writer.write(lonVar, Array.makeFromJavaArray(longitudes));

            for (int v = 0; v < dataVars.size(); v++) {
                final float[][][] data = new float[dates.size()][latitudes.length][longitudes.length];
                for (int t = 0; t < dates.size(); t++) {
                    for (int i = 0; i < latitudes.length; i++) {
                        for (int j = 0; j < longitudes.length; j++) {
                            data[t][i][j] = (float) expectedValue(v, t, i, j);
                        }
                    }
                }
                if (withFillCell && v == 0) {
                    data[0][0][2] = FILL_VALUE;
                }
                writer.write(dataVars.get(v), Array.makeFromJavaArray(data));
            }
        } catch (final InvalidRangeException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

}
