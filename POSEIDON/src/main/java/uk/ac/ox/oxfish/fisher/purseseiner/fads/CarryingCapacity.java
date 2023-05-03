package uk.ac.ox.oxfish.fisher.purseseiner.fads;

public interface CarryingCapacity {
    default boolean isDud() {
        return getTotal() == 0.0;
    }

    double getTotal();

    double[] getCarryingCapacities();
}
