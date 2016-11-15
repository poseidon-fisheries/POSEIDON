package uk.ac.ox.oxfish.utility.bandit.factory;

import com.google.common.base.Supplier;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.bandit.BanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.EpsilonGreedyBanditAlgorithm;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Construct a constructor. Confusing but faster to code this way
 * Created by carrknight on 11/11/16.
 */
public class EpsilonGreedyBanditFactory implements
        AlgorithmFactory<BanditSupplier>{



    private DoubleParameter explorationRate = new FixedDoubleParameter(.2);


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
            public EpsilonGreedyBanditAlgorithm apply(BanditAverage banditAverage) {
                return new EpsilonGreedyBanditAlgorithm(banditAverage,explorationRate.apply(state.getRandom()));
            }
        };
    }

    /**
     * Getter for property 'explorationRate'.
     *
     * @return Value for property 'explorationRate'.
     */
    public DoubleParameter getExplorationRate() {
        return explorationRate;
    }


    /**
     * Setter for property 'explorationRate'.
     *
     * @param explorationRate Value to set for property 'explorationRate'.
     */
    public void setExplorationRate(DoubleParameter explorationRate) {
        this.explorationRate = explorationRate;
    }
}
