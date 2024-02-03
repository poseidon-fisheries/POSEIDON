package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.CompositeDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

public class MaxHoursOutWithRestingTimeDepartingStrategy implements AlgorithmFactory<CompositeDepartingStrategy> {

    private MaxHoursPerYearDepartingFactory firstDelegate = new MaxHoursPerYearDepartingFactory();

    private FixedRestTimeDepartingFactory fixedRest = new FixedRestTimeDepartingFactory();


    @Override
    public CompositeDepartingStrategy apply(FishState fishState) {
        return new CompositeDepartingStrategy(
            firstDelegate.apply(fishState),
            fixedRest.apply(fishState)
        );
    }

    public DoubleParameter getMaxHoursOut() {
        return firstDelegate.getMaxHoursOut();
    }

    public void setMaxHoursOut(DoubleParameter maxHoursOut) {
        firstDelegate.setMaxHoursOut(maxHoursOut);
    }

    public DoubleParameter getHoursBetweenEachDeparture() {
        return fixedRest.getHoursBetweenEachDeparture();
    }

    public void setHoursBetweenEachDeparture(DoubleParameter hoursBetweenEachDeparture) {
        fixedRest.setHoursBetweenEachDeparture(hoursBetweenEachDeparture);
    }


}
