package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class GlobalCarryingCapacityInitializer
    implements CarryingCapacityInitializer<GlobalCarryingCapacity> {

    private final DoubleParameter carryingCapacity;

    public GlobalCarryingCapacityInitializer(
        final DoubleParameter carryingCapacity
    ) {
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public GlobalCarryingCapacity apply(final MersenneTwisterFast rng) {
        return new GlobalCarryingCapacity(
            carryingCapacity.applyAsDouble(rng)
        );
    }
}
