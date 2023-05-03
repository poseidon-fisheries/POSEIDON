package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class GlobalCarryingCapacityInitializer
    extends AbstractCarryingCapacityInitializer<GlobalCarryingCapacity> {

    private final DoubleParameter carryingCapacity;

    public GlobalCarryingCapacityInitializer(
        final double probabilityOfFadBeingDud,
        final DoubleParameter carryingCapacity
    ) {
        super(probabilityOfFadBeingDud);
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    protected GlobalCarryingCapacity makeDud() {
        return new GlobalCarryingCapacity(0);
    }

    @Override
    protected GlobalCarryingCapacity makeCarryingCapacity(final MersenneTwisterFast rng) {
        return new GlobalCarryingCapacity(
            carryingCapacity.applyAsDouble(rng)
        );
    }
}
