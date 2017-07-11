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
