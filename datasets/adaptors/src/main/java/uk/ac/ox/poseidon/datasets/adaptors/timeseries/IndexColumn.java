package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.poseidon.datasets.api.Column;

import java.util.Iterator;

import static com.google.common.collect.Streams.mapWithIndex;

public abstract class IndexColumn<T> implements Column<T> {

    private final String name;
    private final Column<?> indexedColumn;

    IndexColumn(final String name, final Column<?> indexedColumn) {
        this.name = name;
        this.indexedColumn = indexedColumn;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Iterator<T> iterator() {
        return mapWithIndex(indexedColumn.stream(), (from, index) -> mapIndex(index)).iterator();
    }

    protected abstract T mapIndex(final long index);
}
