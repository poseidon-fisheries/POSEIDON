package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public abstract class TimeSeriesDataSetAdaptor implements Dataset {

    private final TimeSeries<?> timeSeries;
    private final String indexColumnName;

    TimeSeriesDataSetAdaptor(
        final TimeSeries<?> timeSeries,
        final String indexColumnName
    ) {
        this.timeSeries = timeSeries;
        this.indexColumnName = indexColumnName;
    }

    String getIndexColumnName() {
        return indexColumnName;
    }

    @Override
    public List<Table> getTables() {
        return dataColumnStream()
            .map(this::makeTable)
            .collect(toImmutableList());
    }

    private Stream<DataColumn> dataColumnStream() {
        return timeSeries.getColumns().stream();
    }

    abstract DataColumnTableAdaptor makeTable(final DataColumn dataColumn);

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

    @Override
    public List<String> getTableNames() {
        return dataColumnStream()
            .map(DataColumn::getName)
            .collect(toImmutableList());
    }
}
