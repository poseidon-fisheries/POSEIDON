package uk.ac.ox.poseidon.simulations.api;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface Scenario {
    Simulation newSimulation();

    Path getInputFolder();

    void setInputFolder(Path path);

    default void setInputFolder(final String first, final String... more) {
        setInputFolder(Paths.get(first, more));
    }
}