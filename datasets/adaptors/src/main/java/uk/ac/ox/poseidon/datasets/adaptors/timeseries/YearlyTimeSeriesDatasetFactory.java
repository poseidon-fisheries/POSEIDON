package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;

@AutoService(DatasetFactory.class)
public class YearlyTimeSeriesDatasetFactory extends TimeSeriesDatasetFactory {
    @Override
    public String getDatasetName() {
        return "Yearly time series";
    }

    @Override
    Dataset makeDataset(final FishState fishState) {
        return new YearlyTimeSeriesDataSetAdaptor(
            getTimeSeries(fishState),
            getIndexColumnName(),
            fishState.getScenario().getStartDate().getYear()
        );
    }

    @Override
    TimeSeries<?> getTimeSeries(final FishState fishState) {
        return fishState.getYearlyDataSet();
    }

    @Override
    String getIndexColumnName() {
        return "year";
    }
}
