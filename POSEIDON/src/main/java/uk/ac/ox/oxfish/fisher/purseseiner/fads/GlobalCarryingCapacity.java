package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.Species;

public class GlobalCarryingCapacity implements CarryingCapacity {
    private final double carryingCapacity;

    public GlobalCarryingCapacity(final double carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public double getTotal() {
        return carryingCapacity;
    }

    @Override
    public boolean isFull(
        final Fad fad,
        final Species species
    ) {
        return fad.getBiology().getTotalBiomass() >= carryingCapacity;
    }

}
