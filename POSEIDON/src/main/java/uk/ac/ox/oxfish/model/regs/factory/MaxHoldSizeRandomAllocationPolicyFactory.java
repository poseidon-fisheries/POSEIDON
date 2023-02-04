/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MaxHoldSizeRandomAllocationPolicy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class MaxHoldSizeRandomAllocationPolicyFactory implements AlgorithmFactory<MaxHoldSizeRandomAllocationPolicy> {



    private DoubleParameter yearlyHoldSizeLimit = new FixedDoubleParameter(10000d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MaxHoldSizeRandomAllocationPolicy apply(FishState state) {
        return new MaxHoldSizeRandomAllocationPolicy(yearlyHoldSizeLimit.apply(state.getRandom()));
    }

    /**
     * Getter for property 'yearlyHoldSizeLimit'.
     *
     * @return Value for property 'yearlyHoldSizeLimit'.
     */
    public DoubleParameter getYearlyHoldSizeLimit() {
        return yearlyHoldSizeLimit;
    }

    /**
     * Setter for property 'yearlyHoldSizeLimit'.
     *
     * @param yearlyHoldSizeLimit Value to set for property 'yearlyHoldSizeLimit'.
     */
    public void setYearlyHoldSizeLimit(DoubleParameter yearlyHoldSizeLimit) {
        this.yearlyHoldSizeLimit = yearlyHoldSizeLimit;
    }
}
