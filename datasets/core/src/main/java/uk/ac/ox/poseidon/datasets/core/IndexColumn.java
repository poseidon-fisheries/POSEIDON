package uk.ac.ox.poseidon.datasets.core;

import uk.ac.ox.poseidon.datasets.api.Column;

import java.util.Iterator;

import static com.google.common.collect.Streams.mapWithIndex;

public class IndexColumn implements Column<Long> {

    private final String name;
    private final Column<?> indexedColumn;

    public IndexColumn(final String name, final Column<?> indexedColumn) {
        this.name = name;
        this.indexedColumn = indexedColumn;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Iterator<Long> iterator() {
        return mapWithIndex(indexedColumn.stream(), (from, index) -> index).iterator();
    }
}
