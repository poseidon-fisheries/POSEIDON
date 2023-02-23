package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

public class DailyTimeSeriesDatasetFactory extends TimeSeriesDatasetFactory {
    @Override
    TimeSeries<?> getTimeSeries(final FishState fishState) {
        return fishState.getDailyDataSet();
    }

    @Override
    String getIndexColumnName() {
        return "step";
    }

    @Override
    String getDatasetName() {
        return "Daily time series";
    }
}
