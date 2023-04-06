package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.function.DoubleSupplier;

public abstract class GlobalCarryingCapacitiesFactory
    extends CarryingCapacitiesFactory
    implements AlgorithmFactory<DoubleSupplier> {

    public GlobalCarryingCapacitiesFactory() {
    }

    @Override
    public DoubleSupplier apply(final FishState fishState) {
        return () -> makeGlobalCarryingCapacityParameter().applyAsDouble(fishState.getRandom());
    }

    abstract DoubleParameter makeGlobalCarryingCapacityParameter();
}
