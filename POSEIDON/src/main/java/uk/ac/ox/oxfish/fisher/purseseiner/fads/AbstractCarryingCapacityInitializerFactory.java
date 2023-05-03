package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.geography.fads.CarryingCapacityInitializerFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public abstract class AbstractCarryingCapacityInitializerFactory<T extends CarryingCapacity>
    implements CarryingCapacityInitializerFactory<T> {

    private DoubleParameter probabilityOfFadBeingDud =
        new CalibratedParameter(0, 0.35, 0, 1, 0.001);

    public AbstractCarryingCapacityInitializerFactory() {
    }

    public AbstractCarryingCapacityInitializerFactory(final DoubleParameter probabilityOfFadBeingDud) {
        this.probabilityOfFadBeingDud = probabilityOfFadBeingDud;
    }

    public DoubleParameter getProbabilityOfFadBeingDud() {
        return probabilityOfFadBeingDud;
    }

    public void setProbabilityOfFadBeingDud(final DoubleParameter probabilityOfFadBeingDud) {
        this.probabilityOfFadBeingDud = probabilityOfFadBeingDud;
    }

}
