package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import uk.ac.ox.oxfish.fisher.strategies.gear.FixedGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * The factory that creates the fixed gear strategy
 * Created by carrknight on 6/13/16.
 */
public class FixedGearStrategyFactory implements AlgorithmFactory<FixedGearStrategy>
{


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedGearStrategy apply(FishState state) {
        return new FixedGearStrategy();
    }

}
