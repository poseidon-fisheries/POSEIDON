package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishUntilFullStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

/***
 *      ___ _   ___ _____ ___  _____   __
 *     | __/_\ / __|_   _/ _ \| _ \ \ / /
 *     | _/ _ \ (__  | || (_) |   /\ V /
 *     |_/_/ \_\___| |_| \___/|_|_\ |_|
 *
 */
public class FishUntilFullFactory implements StrategyFactory<FishUntilFullStrategy>
{

    public FishUntilFullFactory() {
    }

    private double minimumPercentageFull = 1;

    public double getMinimumPercentageFull() {
        return minimumPercentageFull;
    }

    public void setMinimumPercentageFull(double minimumPercentageFull) {
        if(minimumPercentageFull < 0 || minimumPercentageFull > 1)
            System.err.println("Probability has to be in [0,1]. New value is ignored");
        else

            this.minimumPercentageFull = minimumPercentageFull;
    }

    @Override
    public Class<? super FishUntilFullStrategy> getStrategySuperClass() {
        return FishUntilFullStrategy.class;
    }

    @Override
    public FishUntilFullStrategy apply(FishState state) {
        return new FishUntilFullStrategy(getMinimumPercentageFull());
    }
}
