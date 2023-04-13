package uk.ac.ox.oxfish.fisher.purseseiner.fads;

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
    public double[] getCarryingCapacities() {
        throw new UnsupportedOperationException();
    }

}
