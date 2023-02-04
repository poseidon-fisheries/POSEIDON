package uk.ac.ox.poseidon.simulation.api;

public interface Simulation {

    int getStep();

    String getId();

    void step();
}
