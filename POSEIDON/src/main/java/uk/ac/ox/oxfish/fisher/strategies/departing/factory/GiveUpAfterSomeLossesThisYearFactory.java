package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.GiveUpAfterSomeLossesThisYearDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class GiveUpAfterSomeLossesThisYearFactory implements AlgorithmFactory<GiveUpAfterSomeLossesThisYearDecorator> {


    private DoubleParameter howManyBadTripsBeforeGivingUp = new FixedDoubleParameter(3);

    private DoubleParameter minimumProfitPerTripRequired = new FixedDoubleParameter(0);

    private AlgorithmFactory<? extends DepartingStrategy> delegate = new MaxHoursPerYearDepartingFactory(9999999);


    @Override
    public GiveUpAfterSomeLossesThisYearDecorator apply(final FishState state) {
        return new GiveUpAfterSomeLossesThisYearDecorator(
            (int) howManyBadTripsBeforeGivingUp.applyAsDouble(state.getRandom()),
            (int) minimumProfitPerTripRequired.applyAsDouble(state.getRandom()),
            delegate.apply(state)
        );
    }


    public DoubleParameter getHowManyBadTripsBeforeGivingUp() {
        return howManyBadTripsBeforeGivingUp;
    }

    public void setHowManyBadTripsBeforeGivingUp(final DoubleParameter howManyBadTripsBeforeGivingUp) {
        this.howManyBadTripsBeforeGivingUp = howManyBadTripsBeforeGivingUp;
    }

    public DoubleParameter getMinimumProfitPerTripRequired() {
        return minimumProfitPerTripRequired;
    }

    public void setMinimumProfitPerTripRequired(final DoubleParameter minimumProfitPerTripRequired) {
        this.minimumProfitPerTripRequired = minimumProfitPerTripRequired;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getDelegate() {
        return delegate;
    }

    public void setDelegate(final AlgorithmFactory<? extends DepartingStrategy> delegate) {
        this.delegate = delegate;
    }
}
