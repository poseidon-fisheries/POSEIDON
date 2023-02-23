package uk.ac.ox.poseidon.datasets.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

public class YearlyFishStateTimeSeriesDatasetFactory extends FishStateTimeSeriesDatasetFactory {
    @Override
    TimeSeries<?> getTimeSeries(final FishState fishState) {
        return fishState.getYearlyDataSet();
    }

    @Override
    String getIndexColumnName() {
        return "year";
    }

    @Override
    String getDatasetName() {
        return "Yearly time series";
    }
}
