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

import uk.ac.ox.oxfish.biology.initializer.allocator.BoundedConstantAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/11/17.
 */
public class BoundedAllocatorFactory implements AlgorithmFactory<BoundedConstantAllocator> {


    private DoubleParameter lowestX = new FixedDoubleParameter(-100);
    private DoubleParameter lowestY = new FixedDoubleParameter(-100);
    private DoubleParameter highestX = new FixedDoubleParameter(1000);
    private DoubleParameter highestY = new FixedDoubleParameter(1000);
    /**
     * when this is false the allocation happens OUTSIDE the box rather than within it
     */
    private boolean insideTheBox = true;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BoundedConstantAllocator apply(FishState state) {
        return new BoundedConstantAllocator(
                lowestX.apply(state.getRandom()),
                lowestY.apply(state.getRandom()),
                highestX.apply(state.getRandom()),
                highestY.apply(state.getRandom()),
                insideTheBox

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
    public void setLowestX(DoubleParameter lowestX) {
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
    public void setLowestY(DoubleParameter lowestY) {
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
    public void setHighestX(DoubleParameter highestX) {
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
    public void setHighestY(DoubleParameter highestY) {
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
    public void setInsideTheBox(boolean insideTheBox) {
        this.insideTheBox = insideTheBox;
    }
}
