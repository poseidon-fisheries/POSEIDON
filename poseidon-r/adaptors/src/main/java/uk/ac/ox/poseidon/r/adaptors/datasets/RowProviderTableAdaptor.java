/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
package uk.ac.ox.poseidon.r.adaptors.datasets;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.common.core.AbstractAdaptor;
import uk.ac.ox.poseidon.datasets.api.Column;
import uk.ac.ox.poseidon.datasets.api.Row;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Streams.mapWithIndex;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class RowProviderTableAdaptor
    extends AbstractAdaptor<RowProvider>
    implements Table {

    private final int NA_INTEGER = -2147483648;
    private final double NA_REAL = Double.longBitsToDouble(0x7ff00000000007a2L);

    private final BiMap<String, Integer> columnIndices;

    RowProviderTableAdaptor(final RowProvider delegate) {
        super(delegate);
        this.columnIndices =
            mapWithIndex(delegate.getHeaders().stream(), AbstractMap.SimpleImmutableEntry::new)
                .collect(toImmutableBiMap(Map.Entry::getKey, e -> e.getValue().intValue()));
    }

    @Override
    public List<String> getColumnNames() {
        return getDelegate().getHeaders();
    }

    @Override
    public Iterable<Row> getRows() {
        return Streams.stream(getDelegate().getRows())
            .map(RowProviderRow::new)
            .collect(toImmutableList());
    }

    @Override
    public Collection<Column<?>> getColumns() {

        final List<List<Object>> objectLists =
            Stream.generate(ArrayList::new)
                .limit(columnIndices.size())
                .collect(toList());

        Streams
            .stream(getDelegate().getRows())
            .forEach(row ->
                range(0, row.size()).forEach(i ->
                    objectLists.get(i).add(row.get(i))
                )
            );

        return mapWithIndex(
            objectLists.stream(),
            (objectList, index) ->
                makeColumn(
                    columnIndices.inverse().get((int) index),
                    objectList
                )
        ).collect(toImmutableList());
    }

    private Column<?> makeColumn(
        final String name,
        final Collection<Object> objectList
    ) {

        // All integer classes get converted to int, except Long, which R represents as double
        if (allOfClasses(objectList, ImmutableList.of(Integer.class, Short.class, Byte.class))) {
            return new IntegerColumn(
                name,
                objectList
                    .stream()
                    .mapToInt(o -> o == null ? NA_INTEGER : ((Number) o).intValue())
                    .toArray()
            );
        } else if (allOfClass(objectList, Number.class)) {
            return new DoubleColumn(
                name,
                objectList
                    .stream()
                    .mapToDouble(o -> o == null ? NA_REAL : ((Number) o).doubleValue())
                    .toArray()
            );
        } else if (allOfClass(objectList, Boolean.class)) {
            // it looks weird to return a Boolean[] instead of boolean[], but logical vectors
            // in R are implemented as integer arrays, and we can't return one of those directly
            // because rJava would then mistake it as an actual int array, so we return boxed
            // booleans and let rJava do the unboxing into ints by itself.
            return new ArrayColumn<>(
                name,
                objectList
                    .stream()
                    .map(Boolean.class::cast)
                    .toArray(Boolean[]::new)
            );
        } else if (allOfClass(objectList, String.class)) {
            return new ArrayColumn<>(
                name,
                objectList
                    .stream()
                    .map(String.class::cast)
                    .toArray(String[]::new)
            );
        } else if (allOfClass(objectList, LocalDate.class)) {
            return new DateColumn(
                name,
                objectList
                    .stream()
                    .map(LocalDate.class::cast)
                    .mapToDouble(localDate -> (double) localDate.toEpochDay())
                    .toArray()
            );
        } else if (allOfClass(objectList, LocalDateTime.class)) {
            return new DateTimeColumn(
                name,
                objectList
                    .stream()
                    .map(LocalDateTime.class::cast)
                    .mapToDouble(localDateTime ->
                        (double) localDateTime.toEpochSecond(ZoneOffset.UTC)
                    )
                    .toArray()
            );
        } else {
            return new ArrayColumn<>(
                name,
                objectList.toArray()
            );
        }
    }

    private static boolean allOfClasses(
        final Collection<Object> objects,
        final Collection<Class<?>> classes
    ) {
        return objects.stream().allMatch(o ->
            o == null || classes.stream().anyMatch(clazz -> clazz.isInstance(o))
        );
    }

    private static boolean allOfClass(
        final Collection<Object> objects,
        final Class<?> clazz
    ) {
        return objects.stream().allMatch(o -> o == null || clazz.isInstance(o));
    }

    private class RowProviderRow implements Row {

        private final Collection<?> values;

        private RowProviderRow(final Collection<?> values) {
            this.values = values;
        }

        @Override
        public Object getValue(final String columnName) {
            return get(values, columnIndices.get(columnName));
        }
    }
}
