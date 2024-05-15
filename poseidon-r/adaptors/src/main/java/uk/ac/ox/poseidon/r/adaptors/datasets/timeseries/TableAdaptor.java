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
package uk.ac.ox.poseidon.r.adaptors.datasets.timeseries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import uk.ac.ox.poseidon.datasets.api.Column;
import uk.ac.ox.poseidon.datasets.api.Row;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.mapWithIndex;
import static java.util.function.Function.identity;

public class TableAdaptor implements Table {
    private final ImmutableList<String> columnNames;
    private final Map<String, Integer> columnIndices;
    private final ImmutableMap<String, Column<?>> columns;

    TableAdaptor(final Column<?>... columns) {
        checkArgument(columns.length > 0);
        this.columns = Arrays.stream(columns).collect(toImmutableMap(Column::getName, identity()));
        this.columnNames = this.columns.keySet().asList();
        this.columnIndices =
            mapWithIndex(columnNames.stream(), SimpleImmutableEntry::new)
                .collect(toImmutableMap(Entry::getKey, e -> e.getValue().intValue()));
    }

    @Override
    public List<String> getColumnNames() {
        return columnNames;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Iterable<Row> getRows() {
        return () ->
            getColumns()
                .stream()
                .map(col -> col.stream().map(Stream::of))
                .reduce(
                    Stream.generate(Stream::of),
                    (a, b) -> Streams.zip(a, b, Stream::concat)
                )
                .map(s -> (Row) new TableRow(s.toArray()))
                .iterator();
    }

    @Override
    public Collection<Column<?>> getColumns() {
        return columns.values();
    }

    private class TableRow implements Row {

        private final Object[] values;

        public TableRow(final Object[] values) {
            this.values = values;
        }

        @Override
        public Object getValue(final String columnName) {
            checkArgument(columns.containsKey(columnName));
            return values[columnIndices.get(columnName)];
        }
    }

}
