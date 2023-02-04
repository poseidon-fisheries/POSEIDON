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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/11/17.
 */
public class ConstantAllocatorFactory implements AlgorithmFactory<ConstantBiomassAllocator> {


    private DoubleParameter constantValue = new FixedDoubleParameter(1d);


    public ConstantAllocatorFactory() {
    }

    public ConstantAllocatorFactory(double constantValue) {
        this.constantValue = new FixedDoubleParameter(constantValue);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ConstantBiomassAllocator apply(FishState state) {

        return new ConstantBiomassAllocator(constantValue.apply(state.getRandom()));
    }


    /**
     * Getter for property 'constantValue'.
     *
     * @return Value for property 'constantValue'.
     */
    public DoubleParameter getConstantValue() {
        return constantValue;
    }

    /**
     * Setter for property 'constantValue'.
     *
     * @param constantValue Value to set for property 'constantValue'.
     */
    public void setConstantValue(DoubleParameter constantValue) {
        this.constantValue = constantValue;
    }
}
