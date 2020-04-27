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

    private AlgorithmFactory<? extends DepartingStrategy> delegate = new MaxHoursPerYearDepartingFactory();


    @Override
    public GiveUpAfterSomeLossesThisYearDecorator apply(FishState state) {

        return new GiveUpAfterSomeLossesThisYearDecorator(
                howManyBadTripsBeforeGivingUp.apply(state.getRandom()).intValue(),
                minimumProfitPerTripRequired.apply(state.getRandom()).intValue(),
                delegate.apply(state)

        );


    }


    public DoubleParameter getHowManyBadTripsBeforeGivingUp() {
        return howManyBadTripsBeforeGivingUp;
    }

    public void setHowManyBadTripsBeforeGivingUp(DoubleParameter howManyBadTripsBeforeGivingUp) {
        this.howManyBadTripsBeforeGivingUp = howManyBadTripsBeforeGivingUp;
    }

    public DoubleParameter getMinimumProfitPerTripRequired() {
        return minimumProfitPerTripRequired;
    }

    public void setMinimumProfitPerTripRequired(DoubleParameter minimumProfitPerTripRequired) {
        this.minimumProfitPerTripRequired = minimumProfitPerTripRequired;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getDelegate() {
        return delegate;
    }

    public void setDelegate(AlgorithmFactory<? extends DepartingStrategy> delegate) {
        this.delegate = delegate;
    }
}
