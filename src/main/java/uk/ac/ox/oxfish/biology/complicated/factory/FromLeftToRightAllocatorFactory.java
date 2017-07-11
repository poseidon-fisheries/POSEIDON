package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.initializer.allocator.FromLeftToRightBiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/11/17.
 */
public class FromLeftToRightAllocatorFactory implements AlgorithmFactory<FromLeftToRightBiomassAllocator> {


    private DoubleParameter lowestX = new FixedDoubleParameter(-100);
    private DoubleParameter lowestY = new FixedDoubleParameter(-100);
    private DoubleParameter highestX = new FixedDoubleParameter(1000);
    private DoubleParameter highestY = new FixedDoubleParameter(1000);
    private DoubleParameter exponent = new FixedDoubleParameter(2);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FromLeftToRightBiomassAllocator apply(FishState state) {

        return new FromLeftToRightBiomassAllocator(
                lowestX.apply(state.getRandom()),
                lowestY.apply(state.getRandom()),
                highestX.apply(state.getRandom()),
                highestY.apply(state.getRandom()),
                exponent.apply(state.getRandom())
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
     * Getter for property 'exponent'.
     *
     * @return Value for property 'exponent'.
     */
    public DoubleParameter getExponent() {
        return exponent;
    }

    /**
     * Setter for property 'exponent'.
     *
     * @param exponent Value to set for property 'exponent'.
     */
    public void setExponent(DoubleParameter exponent) {
        this.exponent = exponent;
    }
}
