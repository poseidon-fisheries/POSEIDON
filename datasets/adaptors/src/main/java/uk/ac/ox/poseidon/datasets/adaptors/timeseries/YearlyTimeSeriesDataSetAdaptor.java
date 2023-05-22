package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

import static uk.ac.ox.poseidon.datasets.adaptors.timeseries.DataColumnTableAdaptor.VALUE_COLUMN_NAME;

class YearlyTimeSeriesDataSetAdaptor extends TimeSeriesDataSetAdaptor {
    private final int startYear;

    YearlyTimeSeriesDataSetAdaptor(
        final TimeSeries<?> timeSeries,
        final String indexColumnName,
        final int startYear
    ) {
        super(timeSeries, indexColumnName);
        this.startYear = startYear;
    }

    @Override
    DataColumnTableAdaptor makeTable(final DataColumn dataColumn) {
        final DataColumnColumnAdaptor dataColumnAdaptor =
            new DataColumnColumnAdaptor(VALUE_COLUMN_NAME, dataColumn);
        return new DataColumnTableAdaptor(
            new YearlyIndexColumn(
                getIndexColumnName(), dataColumnAdaptor, startYear
            ),
            dataColumnAdaptor
        );
    }
}
