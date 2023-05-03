package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.Services;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;
import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

public class FishStateAdaptor implements Simulation {

    private final FishState fishState;
    private final Map<String, Dataset> datasets;

    FishStateAdaptor(final FishState fishState) {
        this.fishState = fishState;
        this.datasets = Services
            .loadAll(
                DatasetFactory.class,
                datasetFactory -> datasetFactory.isAutoRegistered() && datasetFactory.test(fishState)
            )
            .stream()
            .map(datasetFactory -> datasetFactory.apply(fishState))
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public int getStep() {
        return fishState.getStep();
    }

    @Override
    public String getId() {
        return fishState.getUniqueID();
    }

    @Override
    public void step() {
        if (!fishState.isStarted()) {
            fishState.start();
        }
        fishState.schedule.step(fishState);
    }

    @Override
    public Map<String, Dataset> getDatasets() {
        return datasets;
    }
}
