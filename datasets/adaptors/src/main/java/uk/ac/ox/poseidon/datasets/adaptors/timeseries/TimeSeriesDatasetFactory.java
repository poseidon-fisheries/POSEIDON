package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;

import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public abstract class TimeSeriesDatasetFactory implements DatasetFactory {

    abstract TimeSeries<?> getTimeSeries(FishState fishState);

    abstract String getIndexColumnName();

    @Override
    public Entry<String, Dataset> apply(final Object o) {
        checkArgument(test(o));
        final FishState fishState = (FishState) o;
        return entry(
            getDatasetName(),
            new TimeSeriesDataSetAdaptor(getTimeSeries(fishState), getIndexColumnName())
        );
    }

    @Override
    public boolean test(final Object o) {
        return FishState.class.isAssignableFrom(o.getClass());
    }

    @Override
    public boolean isAutoRegistered() {
        return true;
    }
}
