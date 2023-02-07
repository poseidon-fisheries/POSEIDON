package uk.ac.ox.poseidon.datasets.adaptors;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class TimeSeriesDataSetAdaptor implements Dataset {

    private final TimeSeries<?> timeSeries;
    private final String indexColumnName;

    public TimeSeriesDataSetAdaptor(
        final TimeSeries<?> timeSeries,
        final String indexColumnName
    ) {
        this.timeSeries = timeSeries;
        this.indexColumnName = indexColumnName;
    }

    @Override
    public List<String> getTableNames() {
        return dataColumnStream()
            .map(DataColumn::getName)
            .collect(toImmutableList());
    }

    private Stream<DataColumn> dataColumnStream() {
        return timeSeries.getColumns().stream();
    }

    @Override
    public List<Table> getTables() {
        return dataColumnStream()
            .map(this::makeTable)
            .collect(toImmutableList());
    }

    private DataColumnTableAdaptor makeTable(final DataColumn dataColumn) {
        return new DataColumnTableAdaptor(indexColumnName, dataColumn);
    }

    @Override
    public Table getTable(final String name) {
        return dataColumnStream()
            .filter(dataColumn -> dataColumn.getName().equals(name))
            .findFirst()
            .map(this::makeTable)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format(
                    "Unknown table: %s. Valid tables: %s.",
                    name,
                    getTableNames()
                )
            ));
    }
}
