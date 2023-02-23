package uk.ac.ox.poseidon.datasets.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

public class DailyFishStateTimeSeriesDatasetFactory extends FishStateTimeSeriesDatasetFactory {
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
