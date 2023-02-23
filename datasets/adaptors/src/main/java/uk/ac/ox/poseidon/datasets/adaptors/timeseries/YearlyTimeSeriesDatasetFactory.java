package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

public class YearlyTimeSeriesDatasetFactory extends TimeSeriesDatasetFactory {
    @Override
    TimeSeries<?> getTimeSeries(final FishState fishState) {
        return fishState.getYearlyDataSet();
    }

    @Override
    String getIndexColumnName() {
        return "year";
    }

    @Override
    public String getDatasetName() {
        return "Yearly time series";
    }
}
