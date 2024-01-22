package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.Species;

public interface CarryingCapacity {
    default boolean isDud() {
        return getTotal() == 0.0;
    }

    double getTotal();

    double[] getCarryingCapacities();

    boolean isFull(
        Fad fad,
        Species species
    );
}
