/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class RandomAllocatorFactory implements AlgorithmFactory<RandomAllocator> {


    private DoubleParameter minimum = new FixedDoubleParameter(0);

    private DoubleParameter maximum = new FixedDoubleParameter(1);


    public RandomAllocatorFactory() {
    }

    public RandomAllocatorFactory(double minimum,
                                  double maximum) {
        this.minimum = new FixedDoubleParameter(minimum);
        this.maximum = new FixedDoubleParameter(maximum);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RandomAllocator apply(FishState state) {
        return  new RandomAllocator(
                maximum.apply(state.getRandom()),
                minimum.apply(state.getRandom())
        );
    }
}
