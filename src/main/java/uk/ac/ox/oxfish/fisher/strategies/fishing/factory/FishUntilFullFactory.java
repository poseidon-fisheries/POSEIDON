package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishUntilFullStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/***
 *      ___ _   ___ _____ ___  _____   __
 *     | __/_\ / __|_   _/ _ \| _ \ \ / /
 *     | _/ _ \ (__  | || (_) |   /\ V /
 *     |_/_/ \_\___| |_| \___/|_|_\ |_|
 *
 */
public class FishUntilFullFactory implements AlgorithmFactory<FishUntilFullStrategy>
{

    public FishUntilFullFactory() {
    }

    private DoubleParameter minimumPercentageFull =  new FixedDoubleParameter(1);


    public DoubleParameter getMinimumPercentageFull() {
        return minimumPercentageFull;
    }

    public void setMinimumPercentageFull(DoubleParameter minimumPercentageFull) {
        this.minimumPercentageFull = minimumPercentageFull;
    }

    @Override
    public FishUntilFullStrategy apply(FishState state) {
        return new FishUntilFullStrategy(getMinimumPercentageFull().apply(state.random));
    }
}
