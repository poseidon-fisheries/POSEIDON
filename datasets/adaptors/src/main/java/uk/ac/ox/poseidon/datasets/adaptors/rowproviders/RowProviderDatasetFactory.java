package uk.ac.ox.poseidon.datasets.adaptors.rowproviders;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public abstract class RowProviderDatasetFactory implements DatasetFactory {

    abstract Map<String, Function<FishState, RowProvider>> getRowProviderFactories();

    @Override
    public boolean isAutoRegistered() {
        return true;
    }
    @Override
    public Entry<String, Dataset> apply(final Object o) {
        checkArgument(test(o));
        final FishState fishState = (FishState) o;
        return entry(
            getDatasetName(),
            new RowProvidersDataset(
                fishState, getRowProviderFactories().entrySet().stream().collect(toImmutableMap(
                    Entry::getKey,
                    entry -> entry.getValue().apply(fishState)
                ))
            )
        );
    }

}
