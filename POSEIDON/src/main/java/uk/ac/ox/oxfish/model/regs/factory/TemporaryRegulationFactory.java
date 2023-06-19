package uk.ac.ox.oxfish.model.regs.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.DecoratedObjectFactory;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class TemporaryRegulationFactory
    extends DecoratedObjectFactory<AlgorithmFactory<? extends Regulation>>
    implements AlgorithmFactory<TemporaryRegulation> {

    private DoubleParameter startDay;
    private DoubleParameter endDay;

    @SuppressWarnings("unused")
    public TemporaryRegulationFactory() {
        super();
    }

    public TemporaryRegulationFactory(
        final AlgorithmFactory<? extends Regulation> delegate,
        final int startDay,
        final int endDay
    ) {
        super(delegate);
        this.startDay = new FixedDoubleParameter(startDay);
        this.endDay = new FixedDoubleParameter(endDay);
    }

    @SuppressWarnings("unused")
    public DoubleParameter getStartDay() {
        return startDay;
    }

    @SuppressWarnings("unused")
    public void setStartDay(final DoubleParameter startDay) {
        this.startDay = startDay;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getEndDay() {
        return endDay;
    }

    @SuppressWarnings("unused")
    public void setEndDay(final DoubleParameter endDay) {
        this.endDay = endDay;
    }

    @Override
    public TemporaryRegulation apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new TemporaryRegulation(
            getDelegate().apply(fishState),
            (int) startDay.applyAsDouble(rng),
            (int) endDay.applyAsDouble(rng)
        );
    }
}
