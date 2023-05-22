package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

import java.time.LocalDate;

import static uk.ac.ox.poseidon.datasets.adaptors.timeseries.DataColumnTableAdaptor.VALUE_COLUMN_NAME;

class DailyTimeSeriesDataSetAdaptor extends TimeSeriesDataSetAdaptor {
    private final LocalDate startDate;

    DailyTimeSeriesDataSetAdaptor(
        final TimeSeries<?> timeSeries,
        final String indexColumnName,
        final LocalDate startDate
    ) {
        super(timeSeries, indexColumnName);
        this.startDate = startDate;
    }

    @Override
    DataColumnTableAdaptor makeTable(final DataColumn dataColumn) {
        final DataColumnColumnAdaptor dataColumnAdaptor =
            new DataColumnColumnAdaptor(VALUE_COLUMN_NAME, dataColumn);
        return new DataColumnTableAdaptor(
            new DailyIndexColumn(
                getIndexColumnName(),
                dataColumnAdaptor,
                startDate
            ),
            dataColumnAdaptor
        );
    }
}
