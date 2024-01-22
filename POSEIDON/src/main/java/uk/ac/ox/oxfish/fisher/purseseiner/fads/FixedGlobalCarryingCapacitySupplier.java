package uk.ac.ox.oxfish.fisher.purseseiner.fads;

public class FixedGlobalCarryingCapacitySupplier implements CarryingCapacitySupplier {

    private final GlobalCarryingCapacity carryingCapacity;

    public FixedGlobalCarryingCapacitySupplier(final double carryingCapacity) {
        this(new GlobalCarryingCapacity(carryingCapacity));
    }

    private FixedGlobalCarryingCapacitySupplier(final GlobalCarryingCapacity carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public CarryingCapacity get() {
        return carryingCapacity;
    }
}
