/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.Ordering.natural;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.univocity.parsers.common.record.Record;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.ShapeFileImporterModified;

public class BiomassReallocatorFactory implements AlgorithmFactory<BiomassReallocator> {

    // `equals` and `hashCode` need to be defined to use the objects of this class as cache keys
    // Using the factory object as cache key allows to always return the same object for the
    // same factory configuration.
    private static final Cache<BiomassReallocatorFactory, BiomassReallocator> cache =
        CacheBuilder.newBuilder().build();
    private SpeciesCodes speciesCodes = TunaScenario.speciesCodesSupplier.get();
    private Path biomassDistributionsFilePath = input("biomass_distributions.csv");
    private Path biomassAreaShapeFile = input("iattc_area").resolve("RFB_IATTC.shp");
    private MapExtent mapExtent;
    // Dates are strings because they're loaded from YAML
    private String startDate = "2017-01-01";
    private String endDate = "2017-12-31";

    @Override
    public int hashCode() {
        return Objects
            .hash(
                speciesCodes,
                biomassDistributionsFilePath,
                biomassAreaShapeFile,
                mapExtent,
                startDate,
                endDate
            );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BiomassReallocatorFactory that = (BiomassReallocatorFactory) o;
        return Objects.equals(speciesCodes, that.speciesCodes) && Objects
            .equals(biomassDistributionsFilePath, that.biomassDistributionsFilePath) && Objects
            .equals(biomassAreaShapeFile, that.biomassAreaShapeFile) && Objects
            .equals(mapExtent, that.mapExtent) && Objects.equals(startDate, that.startDate)
            && Objects.equals(endDate, that.endDate);
    }

    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @SuppressWarnings("unused")
    public Path getBiomassDistributionsFilePath() {
        return biomassDistributionsFilePath;
    }

    @SuppressWarnings("unused")
    public void setBiomassDistributionsFilePath(final Path biomassDistributionsFilePath) {
        this.biomassDistributionsFilePath = biomassDistributionsFilePath;
    }

    @SuppressWarnings("unused")
    public Path getBiomassAreaShapeFile() {
        return biomassAreaShapeFile;
    }

    @SuppressWarnings("unused")
    public void setBiomassAreaShapeFile(final Path biomassAreaShapeFile) {
        this.biomassAreaShapeFile = biomassAreaShapeFile;
    }

    @SuppressWarnings("unused")
    public String getStartDate() {
        return startDate;
    }

    @SuppressWarnings("unused")
    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    @SuppressWarnings("unused")
    public String getEndDate() {
        return endDate;
    }

    @SuppressWarnings("unused")
    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }

    @SuppressWarnings("unused")
    public void setMapExtent(final MapExtent mapExtent) {
        this.mapExtent = mapExtent;
    }

    @Override
    public BiomassReallocator apply(final FishState fishState) {
        try {
            return cache.get(this, () -> new BiomassReallocator(
                buildBiomassGrids(),
                (int) DAYS.between(LocalDate.parse(startDate), LocalDate.parse(endDate)) + 1
            ));
        } catch (final ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    private AllocationGrids<String> buildBiomassGrids() {
        checkNotNull(this.speciesCodes);
        checkNotNull(this.mapExtent);
        checkNotNull(this.startDate);
        checkNotNull(this.endDate);
        final LocalDate startDate = LocalDate.parse(this.startDate);
        final LocalDate endDate = LocalDate.parse(this.endDate);

        final Predicate<Coordinate> isInsideBiomassArea =
            Optional.ofNullable(biomassAreaShapeFile)
                .map(BiomassReallocatorFactory::readShapeFile)
                .<Predicate<Coordinate>>map(field -> field::isInsideUnion)
                .orElse(__ -> true);

        final Map<LocalDate, Map<String, List<Record>>> recordsByDateAndSpeciesName =
            parseAllRecords(biomassDistributionsFilePath).stream()
                .filter(r -> {
                    final LocalDate date = LocalDate.parse(r.getString("date"));
                    return (date.isEqual(startDate) || date.isAfter(startDate))
                        && (date.isBefore(endDate) || date.isEqual(endDate));
                })
                .collect(groupingBy(
                    r -> LocalDate.parse(r.getString("date")),
                    groupingBy(r -> speciesCodes.getSpeciesName(r.getString("species_code")))
                ));

        return new AllocationGrids<>(
            recordsByDateAndSpeciesName
                .entrySet()
                .stream()
                .collect(toImmutableSortedMap(
                    natural(),
                    entry -> (int) DAYS.between(startDate, entry.getKey()),
                    entry -> entry.getValue().entrySet().stream().collect(toImmutableMap(
                        Map.Entry::getKey,
                        subEntry -> makeGrid(mapExtent, isInsideBiomassArea, subEntry.getValue())
                    ))
                ))
        );
    }

    @NotNull
    private static GeomVectorField readShapeFile(final Path shapeFile) {
        final GeomVectorField biomassArea = new GeomVectorField();
        try {
            ShapeFileImporterModified
                .read(shapeFile.toUri().toURL(), biomassArea, null, MasonGeometry.class);
        } catch (final FileNotFoundException | MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        biomassArea.computeUnion();
        return biomassArea;
    }

    private static DoubleGrid2D makeGrid(
        final MapExtent mapExtent,
        final Predicate<? super Coordinate> isInsideBiomassArea,
        final Iterable<? extends Record> records
    ) {
        final DoubleGrid2D grid =
            new DoubleGrid2D(mapExtent.getGridWidth(), mapExtent.getGridHeight());
        records.forEach(record -> {
            final double lon = record.getDouble("lon");
            final double lat = record.getDouble("lat");
            final Coordinate coordinate = new Coordinate(lon, lat);
            if (isInsideBiomassArea.test(coordinate)) {
                grid.set(mapExtent.toGridX(lon), mapExtent.toGridY(lat), record.getDouble("value"));
            }
        });
        return normalize(grid);
    }

    /**
     * Returns a copy of {@code grid} where the values sum up to 1.0
     */
    static DoubleGrid2D normalize(final DoubleGrid2D grid) {
        final double sum = Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum();
        return new DoubleGrid2D(grid).multiply(1 / sum);
    }
}
