/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
                return new EpsilonGreedyBanditAlgorithm(banditAverage,explorationRate.applyAsDouble(state.getRandom()));
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
