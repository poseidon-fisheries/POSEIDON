package uk.ac.ox.poseidon.simulations.api;

import uk.ac.ox.poseidon.datasets.api.Dataset;

import java.util.Map;

public interface Simulation {

    int getStep();

    String getId();

    void step();

    Map<String, Dataset> getDatasets();
}
