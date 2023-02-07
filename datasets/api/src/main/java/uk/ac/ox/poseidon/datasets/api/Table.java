package uk.ac.ox.poseidon.datasets.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface Table extends Iterable<Row> {
    @Override
    default Iterator<Row> iterator() {
        return getRows().iterator();
    }

    List<String> getColumnNames();

    Iterable<Row> getRows();

    Collection<Column<?>> getColumns();
}
