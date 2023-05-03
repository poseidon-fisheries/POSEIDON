package uk.ac.ox.poseidon.simulations.api;

public interface Parameter {
    String getName();

    Object getValue();

    void setValue(Object value);
}
