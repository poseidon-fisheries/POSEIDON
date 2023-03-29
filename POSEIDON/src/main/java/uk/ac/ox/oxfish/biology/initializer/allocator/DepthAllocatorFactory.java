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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/11/17.
 */
public class DepthAllocatorFactory implements AlgorithmFactory<DepthAllocatorDecorator> {


    private DoubleParameter minDepth = new FixedDoubleParameter(800);
    private DoubleParameter maxDepth = new FixedDoubleParameter(1500);


    private AlgorithmFactory<? extends BiomassAllocator> delegate =
        new BoundedAllocatorFactory(62, 27, 95, 34, true);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DepthAllocatorDecorator apply(final FishState state) {
        return new DepthAllocatorDecorator(
            minDepth.applyAsDouble(state.getRandom()),
            maxDepth.applyAsDouble(state.getRandom()),
            delegate.apply(state)

        );
    }


    /**
     * Getter for property 'minDepth'.
     *
     * @return Value for property 'minDepth'.
     */
    public DoubleParameter getMinDepth() {
        return minDepth;
    }

    /**
     * Setter for property 'minDepth'.
     *
     * @param minDepth Value to set for property 'minDepth'.
     */
    public void setMinDepth(final DoubleParameter minDepth) {
        this.minDepth = minDepth;
    }

    /**
     * Getter for property 'maxDepth'.
     *
     * @return Value for property 'maxDepth'.
     */
    public DoubleParameter getMaxDepth() {
        return maxDepth;
    }

    /**
     * Setter for property 'maxDepth'.
     *
     * @param maxDepth Value to set for property 'maxDepth'.
     */
    public void setMaxDepth(final DoubleParameter maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getDelegate() {
        return delegate;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param delegate Value to set for property 'delegate'.
     */
    public void setDelegate(
        final AlgorithmFactory<? extends BiomassAllocator> delegate
    ) {
        this.delegate = delegate;
    }
}
