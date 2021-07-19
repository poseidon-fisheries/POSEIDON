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
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.univocity.parsers.common.record.Record;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class AbstractAllocationGridsFactory<K>
    implements AlgorithmFactory<AllocationGrids<K>> {

    private Path speciesCodesFilePath;
    private Path gridsFilePath;
    private MapExtent mapExtent;
    private final LoadingCache<AbstractAllocationGridsFactory<K>, AllocationGrids<K>> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(__ -> readGridsFromFile()));

    AbstractAllocationGridsFactory(
        final Path speciesCodesFilePath,
        final Path gridsFilePath
    ) {
        this.speciesCodesFilePath = speciesCodesFilePath;
        this.gridsFilePath = gridsFilePath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(speciesCodesFilePath, gridsFilePath, mapExtent);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbstractAllocationGridsFactory<?> that = (AbstractAllocationGridsFactory<?>) o;
        return Objects.equals(speciesCodesFilePath, that.speciesCodesFilePath)
            && Objects.equals(gridsFilePath, that.gridsFilePath)
            && Objects.equals(mapExtent, that.mapExtent);
    }

    @SuppressWarnings("unused")
    public Path getSpeciesCodesFilePath() {
        return speciesCodesFilePath;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodesFilePath(final Path speciesCodesFilePath) {
        this.speciesCodesFilePath = speciesCodesFilePath;
    }

    @SuppressWarnings("unused")
    public Path getGridsFilePath() {
        return gridsFilePath;
    }

    @SuppressWarnings("unused")
    public void setGridsFilePath(final Path gridsFilePath) {
        this.gridsFilePath = gridsFilePath;
    }

    @Override
    public AllocationGrids<K> apply(final FishState fishState) {
        this.mapExtent = new MapExtent(fishState.getMap());
        return cache.getUnchecked(this);
    }

    private AllocationGrids<K> readGridsFromFile() {

        checkNotNull(this.gridsFilePath);
        checkNotNull(this.speciesCodesFilePath);
        checkNotNull(this.mapExtent);

        final SpeciesCodes speciesCodes =
            new SpeciesCodesFromFileFactory(getSpeciesCodesFilePath()).get();

        final Map<LocalDate, Map<K, List<Record>>> recordsByDateAndKey =
            parseAllRecords(gridsFilePath).stream()
                .collect(groupingBy(
                    r -> LocalDate.parse(r.getString("date")),
                    groupingBy(record -> extractKeyFromRecord(speciesCodes, record))
                ));

        final LocalDate startDate = recordsByDateAndKey
            .keySet()
            .stream()
            .min(naturalOrder())
            .orElseThrow(() ->
                new IllegalStateException("No records found in file " + gridsFilePath)
            );

        return AllocationGrids.from(
            recordsByDateAndKey
                .entrySet()
                .stream()
                .collect(toImmutableSortedMap(
                    natural(),
                    entry -> (int) DAYS.between(startDate, entry.getKey()),
                    entry -> entry.getValue().entrySet().stream().collect(toImmutableMap(
                        Map.Entry::getKey,
                        subEntry -> makeGrid(mapExtent, subEntry.getValue())
                    ))
                ))
        );
    }

    abstract K extractKeyFromRecord(SpeciesCodes speciesCodes, Record record);

    private static DoubleGrid2D makeGrid(
        final MapExtent mapExtent,
        final Iterable<? extends Record> records
    ) {
        final DoubleGrid2D grid = new DoubleGrid2D(
            mapExtent.getGridWidth(),
            mapExtent.getGridHeight()
        );
        records.forEach(record -> {
            final double lon = record.getDouble("lon");
            final double lat = record.getDouble("lat");
            grid.set(
                mapExtent.toGridX(lon),
                mapExtent.toGridY(lat),
                record.getDouble("value")
            );
        });
        return normalize(grid);
    }

    /**
     * Returns a copy of {@code grid} where the values sum up to 1.0
     */
    private static DoubleGrid2D normalize(final DoubleGrid2D grid) {
        final double sum = Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum();
        return new DoubleGrid2D(grid).multiply(1 / sum);
    }

}
