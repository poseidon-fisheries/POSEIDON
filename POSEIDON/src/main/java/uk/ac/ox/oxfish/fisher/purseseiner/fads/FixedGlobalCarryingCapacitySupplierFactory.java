package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

public class FixedGlobalCarryingCapacitySupplierFactory implements AlgorithmFactory<CarryingCapacitySupplier> {

    private DoubleParameter carryingCapacity;

    @SuppressWarnings("unused")
    public FixedGlobalCarryingCapacitySupplierFactory() {
    }

    public FixedGlobalCarryingCapacitySupplierFactory(final DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(final DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public CarryingCapacitySupplier apply(final FishState fishState) {
        return new FixedGlobalCarryingCapacitySupplier(carryingCapacity.applyAsDouble(fishState.getRandom()));
    }
}
