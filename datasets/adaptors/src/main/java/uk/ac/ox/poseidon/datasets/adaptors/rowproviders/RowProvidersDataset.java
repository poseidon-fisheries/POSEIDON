package uk.ac.ox.poseidon.datasets.adaptors.rowproviders;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.datasets.adaptors.TableMapDataset;

import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class RowProvidersDataset extends TableMapDataset {

    public RowProvidersDataset(
        final FishState fishState,
        final Map<String, RowProvider> rowProviders
    ) {
        super(
            rowProviders.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> new RowProviderTableAdaptor(entry.getValue())
            ))
        );
        rowProviders.forEach((s, rowProvider) -> {
            if (rowProvider instanceof Startable) {
                fishState.registerStartable((Startable) rowProvider);
            }
        });
    }

}
