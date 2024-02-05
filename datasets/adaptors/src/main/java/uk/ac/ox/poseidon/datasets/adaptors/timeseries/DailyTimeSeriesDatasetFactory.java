package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;

@AutoService(DatasetFactory.class)
public class DailyTimeSeriesDatasetFactory extends TimeSeriesDatasetFactory {
    @Override
    Dataset makeDataset(final FishState fishState) {
        return new DailyTimeSeriesDataSetAdaptor(
            getTimeSeries(fishState),
            getIndexColumnName(),
            fishState.getScenario().getStartDate()
        );
    }

    @Override
    TimeSeries<?> getTimeSeries(final FishState fishState) {
        return fishState.getDailyDataSet();
    }

    @Override
    String getIndexColumnName() {
        return "step";
    }

    @Override
    public String getDatasetName() {
        return "Daily time series";
    }
}
