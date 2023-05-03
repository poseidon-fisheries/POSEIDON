package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.poseidon.datasets.api.Column;

import java.util.Iterator;

public class DataColumnColumnAdaptor implements Column<Double> {

    private final String name;
    private final DataColumn dataColumn;

    public DataColumnColumnAdaptor(final String name, final DataColumn dataColumn) {
        this.name = name;
        this.dataColumn = dataColumn;
    }

    @Override
    public Iterator<Double> iterator() {
        return dataColumn.iterator();
    }

    @Override
    public String getName() {
        return name;
    }
}
