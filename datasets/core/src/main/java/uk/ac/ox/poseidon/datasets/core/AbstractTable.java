package uk.ac.ox.poseidon.datasets.core;

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

public abstract class AbstractTable implements Table {
    private final ImmutableList<String> columnNames;
    private final Map<String, Integer> columnIndices;
    private final ImmutableMap<String, Column<?>> columns;

    public AbstractTable(final Column<?>... columns) {
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

    @Override
    public Collection<Column<?>> getColumns() {
        return columns.values();
    }

    @SuppressWarnings("UnstableApiUsage")
    public Iterable<Row> getRows() {
        return () -> getColumns().stream()
            .map(col -> col.stream().map(Stream::of))
            .reduce(
                Stream.generate(Stream::of),
                (a, b) -> Streams.zip(a, b, Stream::concat)
            )
            .map(s -> (Row) new TableRow(s.toArray()))
            .iterator();
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
