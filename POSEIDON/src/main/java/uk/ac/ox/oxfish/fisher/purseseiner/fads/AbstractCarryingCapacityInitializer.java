package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;

public abstract class AbstractCarryingCapacityInitializer<T extends CarryingCapacity>
    implements CarryingCapacityInitializer<T> {

    private final double probabilityOfFadBeingDud;

    AbstractCarryingCapacityInitializer(final double probabilityOfFadBeingDud) {
        this.probabilityOfFadBeingDud = probabilityOfFadBeingDud;
    }

    @Override
    public T apply(final MersenneTwisterFast rng) {
        return rng.nextDouble() <= probabilityOfFadBeingDud
            ? makeDud()
            : makeCarryingCapacity(rng);
    }

    protected abstract T makeDud();

    protected abstract T makeCarryingCapacity(MersenneTwisterFast rng);

}
