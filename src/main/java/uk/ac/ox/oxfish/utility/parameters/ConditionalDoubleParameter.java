package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;

/**
 * Basically a fixed double parameter that returns NaN if the flag is not active
 * Created by carrknight on 1/28/17.
 */
public class ConditionalDoubleParameter implements DoubleParameter {


    private boolean active = false;

    private DoubleParameter value = new FixedDoubleParameter(0);


    public ConditionalDoubleParameter(boolean active, DoubleParameter value) {
        this.active = active;
        this.value = value;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new ConditionalDoubleParameter(active,value.makeCopy());
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public Double apply(MersenneTwisterFast mersenneTwisterFast) {
        if(active)
            return value.apply(mersenneTwisterFast);
        else
            return Double.NaN;
    }


    /**
     * Getter for property 'active'.
     *
     * @return Value for property 'active'.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setter for property 'active'.
     *
     * @param active Value to set for property 'active'.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     */
    public DoubleParameter getValue() {
        return value;
    }

    /**
     * Setter for property 'value'.
     *
     * @param value Value to set for property 'value'.
     */
    public void setValue(DoubleParameter value) {
        this.value = value;
    }
}
