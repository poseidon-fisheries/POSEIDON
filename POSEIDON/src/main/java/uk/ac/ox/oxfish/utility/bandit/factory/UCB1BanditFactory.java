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
