package uk.ac.ox.poseidon.datasets.adaptors.rowproviders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.common.core.AbstractAdaptor;
import uk.ac.ox.poseidon.datasets.api.Column;
import uk.ac.ox.poseidon.datasets.api.Row;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Streams.mapWithIndex;
import static com.google.common.collect.Streams.stream;

public class RowProviderTableAdaptor extends AbstractAdaptor<RowProvider> implements Table {

    private final Map<String, Integer> columnIndices;
    private final ImmutableList<Column<?>> columns;

    protected RowProviderTableAdaptor(final RowProvider delegate) {
        super(delegate);
        this.columnIndices =
            mapWithIndex(delegate.getHeaders().stream(), SimpleImmutableEntry::new)
                .collect(toImmutableMap(Entry::getKey, e -> e.getValue().intValue()));
        this.columns = columnIndices.entrySet().stream()
            .map(entry -> new RowProviderColumn(entry.getKey(), entry.getValue()))
            .collect(toImmutableList());
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
        return columns;
    }

    private class RowProviderColumn implements Column<Object> {
        private final String name;
        private final int index;

        public RowProviderColumn(final String name, final int index) {
            this.name = name;
            this.index = index;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Iterator<Object> iterator() {
            return Streams.stream(getDelegate().getRows())
                .map(r -> (Object) get(r, index))
                .iterator();
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
