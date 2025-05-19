/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/11/17.
 */
public class BoundedAllocatorFactory implements AlgorithmFactory<BoundedAllocatorDecorator> {


    private DoubleParameter lowestX = new FixedDoubleParameter(-100);
    private DoubleParameter lowestY = new FixedDoubleParameter(-100);
    private DoubleParameter highestX = new FixedDoubleParameter(1000);
    private DoubleParameter highestY = new FixedDoubleParameter(1000);
    /**
     * when this is false the allocation happens OUTSIDE the box rather than within it
     */
    private boolean insideTheBox = true;

    private AlgorithmFactory<? extends BiomassAllocator> delegate = new ConstantAllocatorFactory();


    public BoundedAllocatorFactory() {
    }

    public BoundedAllocatorFactory(
        final double lowestX, final double lowestY, final double highestX,
        final double highestY, final boolean insideTheBox
    ) {
        this.lowestX = new FixedDoubleParameter(lowestX);
        this.lowestY = new FixedDoubleParameter(lowestY);
        this.highestX = new FixedDoubleParameter(highestX);
        this.highestY = new FixedDoubleParameter(highestY);
        this.insideTheBox = insideTheBox;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BoundedAllocatorDecorator apply(final FishState state) {
        return new BoundedAllocatorDecorator(
            lowestX.applyAsDouble(state.getRandom()),
            lowestY.applyAsDouble(state.getRandom()),
            highestX.applyAsDouble(state.getRandom()),
            highestY.applyAsDouble(state.getRandom()),
            insideTheBox,
            delegate.apply(state)

        );
    }

    /**
     * Getter for property 'lowestX'.
     *
     * @return Value for property 'lowestX'.
     */
    public DoubleParameter getLowestX() {
        return lowestX;
    }

    /**
     * Setter for property 'lowestX'.
     *
     * @param lowestX Value to set for property 'lowestX'.
     */
    public void setLowestX(final DoubleParameter lowestX) {
        this.lowestX = lowestX;
    }

    /**
     * Getter for property 'lowestY'.
     *
     * @return Value for property 'lowestY'.
     */
    public DoubleParameter getLowestY() {
        return lowestY;
    }

    /**
     * Setter for property 'lowestY'.
     *
     * @param lowestY Value to set for property 'lowestY'.
     */
    public void setLowestY(final DoubleParameter lowestY) {
        this.lowestY = lowestY;
    }

    /**
     * Getter for property 'highestX'.
     *
     * @return Value for property 'highestX'.
     */
    public DoubleParameter getHighestX() {
        return highestX;
    }

    /**
     * Setter for property 'highestX'.
     *
     * @param highestX Value to set for property 'highestX'.
     */
    public void setHighestX(final DoubleParameter highestX) {
        this.highestX = highestX;
    }

    /**
     * Getter for property 'highestY'.
     *
     * @return Value for property 'highestY'.
     */
    public DoubleParameter getHighestY() {
        return highestY;
    }

    /**
     * Setter for property 'highestY'.
     *
     * @param highestY Value to set for property 'highestY'.
     */
    public void setHighestY(final DoubleParameter highestY) {
        this.highestY = highestY;
    }

    /**
     * Getter for property 'insideTheBox'.
     *
     * @return Value for property 'insideTheBox'.
     */
    public boolean isInsideTheBox() {
        return insideTheBox;
    }

    /**
     * Setter for property 'insideTheBox'.
     *
     * @param insideTheBox Value to set for property 'insideTheBox'.
     */
    public void setInsideTheBox(final boolean insideTheBox) {
        this.insideTheBox = insideTheBox;
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
