package uk.ac.ox.oxfish.utility.bandit.factory;


import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.UCB1BanditAlgorithm;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

public class UCB1BanditFactory implements
        AlgorithmFactory< BanditSupplier>
{


    private DoubleParameter minimumReward = new FixedDoubleParameter(-20);

    private DoubleParameter maximumReward = new FixedDoubleParameter(20);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BanditSupplier apply(FishState state) {
        return new BanditSupplier() {
            @Override
            public UCB1BanditAlgorithm apply(BanditAverage banditAverage) {
                return new UCB1BanditAlgorithm(minimumReward.apply(state.getRandom()),
                                               maximumReward.apply(state.getRandom()),
                                               banditAverage);
            }
        };
    }

    /**
     * Getter for property 'minimumReward'.
     *
     * @return Value for property 'minimumReward'.
     */
    public DoubleParameter getMinimumReward() {
        return minimumReward;
    }

    /**
     * Setter for property 'minimumReward'.
     *
     * @param minimumReward Value to set for property 'minimumReward'.
     */
    public void setMinimumReward(DoubleParameter minimumReward) {
        this.minimumReward = minimumReward;
    }

    /**
     * Getter for property 'maximumReward'.
     *
     * @return Value for property 'maximumReward'.
     */
    public DoubleParameter getMaximumReward() {
        return maximumReward;
    }

    /**
     * Setter for property 'maximumReward'.
     *
     * @param maximumReward Value to set for property 'maximumReward'.
     */
    public void setMaximumReward(DoubleParameter maximumReward) {
        this.maximumReward = maximumReward;
    }
}
