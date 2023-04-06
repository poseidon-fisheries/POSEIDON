package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class CarryingCapacitiesFactory {
    private DoubleParameter probabilityOfFadBeingDud =
        new CalibratedParameter(0, 0.35, 0, 1, 0.001);

    public CarryingCapacitiesFactory() {
    }

    public CarryingCapacitiesFactory(final DoubleParameter probabilityOfFadBeingDud) {
        this.probabilityOfFadBeingDud = probabilityOfFadBeingDud;
    }

    DoubleParameter makeCarryingCapacityParameter(
        final MersenneTwisterFast rng,
        final Supplier<? extends DoubleParameter> parameterIfNotDud
    ) {
        return rng.nextDouble() <= getProbabilityOfFadBeingDud().applyAsDouble(rng)
            ? new FixedDoubleParameter(0)
            : parameterIfNotDud.get();
    }

    public DoubleParameter getProbabilityOfFadBeingDud() {
        return probabilityOfFadBeingDud;
    }

    public void setProbabilityOfFadBeingDud(final DoubleParameter probabilityOfFadBeingDud) {
        this.probabilityOfFadBeingDud = checkNotNull(probabilityOfFadBeingDud);
    }
}
