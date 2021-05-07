package uk.ac.ox.oxfish.model.regs.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class TemporaryRegulationFactory implements AlgorithmFactory<TemporaryRegulation> {

    private DoubleParameter startDay;
    private DoubleParameter endDay;
    private AlgorithmFactory<? extends Regulation> delegate;
    private AlgorithmFactory<? extends Regulation> inactiveDelegate = new AnarchyFactory();

    public TemporaryRegulationFactory(int startDay, int endDay, AlgorithmFactory<? extends Regulation> delegate) {
        this.startDay = new FixedDoubleParameter(startDay);
        this.endDay = new FixedDoubleParameter(endDay);
        this.delegate = delegate;
    }

    @SuppressWarnings("unused") public TemporaryRegulationFactory() {
        this(1, 1, new ProtectedAreasOnlyFactory());
    }

    @SuppressWarnings("unused") public DoubleParameter getStartDay() { return startDay; }
    @SuppressWarnings("unused") public void setStartDay(DoubleParameter startDay) { this.startDay = startDay; }
    @SuppressWarnings("unused") public DoubleParameter getEndDay() { return endDay; }
    @SuppressWarnings("unused") public void setEndDay(DoubleParameter endDay) { this.endDay = endDay; }
    public AlgorithmFactory<? extends Regulation> getDelegate() { return delegate; }
    public void setDelegate(AlgorithmFactory<? extends Regulation> delegate) { this.delegate = delegate; }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends Regulation> getInactiveDelegate() {
        return inactiveDelegate;
    }
    @SuppressWarnings("unused")
    public void setInactiveDelegate(AlgorithmFactory<? extends Regulation> inactiveDelegate) {
        this.inactiveDelegate = inactiveDelegate;
    }

    @Override public TemporaryRegulation apply(FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new TemporaryRegulation(
            startDay.apply(rng).intValue(),
            endDay.apply(rng).intValue(),
            delegate.apply(fishState)
        );
    }
}
