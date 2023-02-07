package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.Services;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetsFactory;
import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.util.Map;

public class FishStateAdaptor implements Simulation {

    private final FishState fishState;
    private final Map<String, Dataset> datasets;

    FishStateAdaptor(final FishState fishState) {
        this.fishState = fishState;
        this.datasets =
            Services.loadFirst(
                DatasetsFactory.class,
                datasetsFactory -> datasetsFactory.test(fishState)
            ).apply(fishState);
    }

    @Override
    public int getStep() {
        return fishState.getStep();
    }

    @Override
    public String getId() {
        return fishState.getTrulyUniqueID();
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
