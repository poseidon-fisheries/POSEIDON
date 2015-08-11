package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.FixedRestTimeDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 *creates fixed rest time departing strategies
 */
public class FixedRestTimeDepartingFactory implements AlgorithmFactory<FixedRestTimeDepartingStrategy>{


    private DoubleParameter hoursBetweenEachDeparture = new FixedDoubleParameter(12.0);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedRestTimeDepartingStrategy apply(FishState state)
    {
        return new FixedRestTimeDepartingStrategy(hoursBetweenEachDeparture.apply(state.getRandom()));
    }


    public DoubleParameter getHoursBetweenEachDeparture() {
        return hoursBetweenEachDeparture;
    }

    public void setHoursBetweenEachDeparture(DoubleParameter hoursBetweenEachDeparture) {
        this.hoursBetweenEachDeparture = hoursBetweenEachDeparture;
    }
}
