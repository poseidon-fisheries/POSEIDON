package uk.ac.ox.poseidon.datasets.adaptors;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.poseidon.datasets.core.AbstractTable;
import uk.ac.ox.poseidon.datasets.core.IndexColumn;

public class DataColumnTableAdaptor extends AbstractTable {
    static String VALUE_COLUMN_NAME = "value";

    public DataColumnTableAdaptor(
        final String indexColumnName,
        final DataColumn dataColumn
    ) {
        this(indexColumnName, new DataColumnColumnAdaptor(VALUE_COLUMN_NAME, dataColumn));
    }

    private DataColumnTableAdaptor(
        final String indexColumnName,
        final DataColumnColumnAdaptor valueColumn
    ) {
        this(new IndexColumn(indexColumnName, valueColumn), valueColumn);
    }

    private DataColumnTableAdaptor(
        final IndexColumn indexColumn,
        final DataColumnColumnAdaptor valueColumn
    ) {
        super(indexColumn, valueColumn);
    }

}
