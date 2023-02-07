package uk.ac.ox.poseidon.datasets.adaptors;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetsFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class FishStateDatasetsFactory implements DatasetsFactory {

    public static final String DAILY_DATASET_NAME = "Daily";
    public static final String YEARLY_DATASET_NAME = "Yearly";

    @Override
    public Map<String, Dataset> apply(final Object o) {
        checkArgument(test(o));
        final FishState fishState = (FishState) o;
        return ImmutableMap.of(
            DAILY_DATASET_NAME, new TimeSeriesDataSetAdaptor(fishState.getDailyDataSet(), "step"),
            YEARLY_DATASET_NAME, new TimeSeriesDataSetAdaptor(fishState.getYearlyDataSet(), "year")
        );
    }

    @Override
    public boolean test(final Object o) {
        return FishState.class.isAssignableFrom(o.getClass());
    }
}
