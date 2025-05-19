/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.environment;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.univocity.parsers.common.record.Record;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.Ordering.natural;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

abstract class AbstractGridsSupplier<K>
    implements Supplier<GenericGrids<K>> {

    @Nullable
//    private final SpeciesCodes speciesCodes;
    private final Path gridsFilePath;
    private final MapExtent mapExtent;
    private final int period;

    private final boolean toNormalize;

    private final LoadingCache<AbstractGridsSupplier<K>, GenericGrids<K>> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(__ -> readGridsFromFile()));

    AbstractGridsSupplier(
        @Nullable

        final SpeciesCodes speciesCodes,
        final Path gridsFilePath,
        final MapExtent mapExtent,
        final int period,
        final boolean toNormalize
    ) {
        //       this.speciesCodes = speciesCodes;
        this.gridsFilePath = gridsFilePath;
        this.mapExtent = mapExtent;
        this.period = period;
        this.toNormalize = toNormalize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gridsFilePath, mapExtent, period);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbstractGridsSupplier<?> that = (AbstractGridsSupplier<?>) o;
        return period == that.period && Objects.equals(gridsFilePath, that.gridsFilePath)
            && Objects.equals(mapExtent, that.mapExtent);
    }

    @Override
    public GenericGrids<K> get() {
        return cache.getUnchecked(this);
    }

    private GenericGrids<K> readGridsFromFile() {

        checkNotNull(this.gridsFilePath);
        checkNotNull(this.mapExtent);

        final Map<LocalDate, Map<K, List<Record>>> recordsByDateAndKey =
            recordStream(gridsFilePath)
                .collect(groupingBy(
                    r -> LocalDate.parse(r.getString("date")),
                    groupingBy(record -> extractKeyFromRecord(record))
                ));

        final LocalDate startDate = recordsByDateAndKey
            .keySet()
            .stream()
            .min(naturalOrder())
            .orElseThrow(() ->
                new IllegalStateException("No records found in file " + gridsFilePath)
            );

        return GenericGrids.from(
            recordsByDateAndKey
                .entrySet()
                .stream()
                .collect(toImmutableSortedMap(
                    natural(),
                    entry -> (int) DAYS.between(startDate, entry.getKey()),
                    entry -> entry.getValue().entrySet().stream().collect(toImmutableMap(
                        Map.Entry::getKey,
                        subEntry -> makeGrid(mapExtent, subEntry.getValue(), toNormalize)
                    ))
                )),
            period
        );
    }

    abstract K extractKeyFromRecord(Record record);

    private static DoubleGrid2D makeGrid(
        final MapExtent mapExtent,
        final Iterable<? extends Record> records,
        final boolean normalize
    ) {
        final DoubleGrid2D grid = new DoubleGrid2D(
            mapExtent.getGridWidth(),
            mapExtent.getGridHeight()
        );
        records.forEach(record -> {
            final double lon = record.getDouble("lon");
            final double lat = record.getDouble("lat");
            final int x = mapExtent.toGridX(lon);
            final int y = mapExtent.toGridY(lat);
            if (x < grid.getWidth() && y < grid.getHeight() &&
                x >= 0 && y >= 0) {
                grid.set(
                    x,
                    y,
                    record.getDouble("value")
                );
            } else {
                // System.err.println( "grid cannot include the point at " + lon + "," + lat + " because it is out of
                // bounds");
            }
        });
        return grid;
    }
}
