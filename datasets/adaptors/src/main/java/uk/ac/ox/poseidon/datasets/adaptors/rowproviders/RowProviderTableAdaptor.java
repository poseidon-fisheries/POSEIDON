/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) -2024 CoHESyS Lab cohesys.lab@gmail.com
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
package uk.ac.ox.poseidon.datasets.adaptors.rowproviders;

import com.google.common.collect.BiMap;
import com.google.common.collect.Streams;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.common.core.AbstractAdaptor;
import uk.ac.ox.poseidon.datasets.api.Column;
import uk.ac.ox.poseidon.datasets.api.Row;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Streams.mapWithIndex;
import static com.google.common.collect.Streams.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class RowProviderTableAdaptor extends AbstractAdaptor<RowProvider> implements Table {

    private final BiMap<String, Integer> columnIndices;

    RowProviderTableAdaptor(final RowProvider delegate) {
        super(delegate);
        this.columnIndices =
            mapWithIndex(delegate.getHeaders().stream(), SimpleImmutableEntry::new)
                .collect(toImmutableBiMap(Entry::getKey, e -> e.getValue().intValue()));
    }

    @Override
    public List<String> getColumnNames() {
        return getDelegate().getHeaders();
    }

    @Override
    public Iterable<Row> getRows() {
        return stream(getDelegate().getRows())
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
                new RowProviderColumn(
                    columnIndices.inverse().get((int) index),
                    unmodifiableList(objectList) // cannot be ImmutableList, as we might have nulls
                )
        ).collect(toImmutableList());
    }

    private static class RowProviderColumn implements Column<Object> {
        private final String name;
        private final List<Object> objects;

        RowProviderColumn(
            final String name,
            final List<Object> objects
        ) {
            this.name = name;
            this.objects = objects;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Iterator<Object> iterator() {
            return objects.iterator();
        }
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
