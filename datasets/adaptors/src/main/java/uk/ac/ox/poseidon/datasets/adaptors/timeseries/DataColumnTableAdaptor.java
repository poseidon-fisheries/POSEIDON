package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.poseidon.datasets.core.AbstractTable;

class DataColumnTableAdaptor extends AbstractTable {
    static String VALUE_COLUMN_NAME = "value";

    DataColumnTableAdaptor(
        final IndexColumn<?> indexColumn,
        final DataColumnColumnAdaptor valueColumn
    ) {
        super(indexColumn, valueColumn);
    }

}
