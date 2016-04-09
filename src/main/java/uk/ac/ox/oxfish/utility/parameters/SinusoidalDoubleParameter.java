package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;

/**
 * Not really random, just a A*sin(frequency * 2Pi*x) generator with x increased by one each
 * time it is called
 * Created by carrknight on 4/8/16.
 */
public class SinusoidalDoubleParameter implements DoubleParameter {


    private double amplitude = 1;

    private double frequency = 1;

    private double step = 0;


    public SinusoidalDoubleParameter(double amplitude, double frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public Double apply(MersenneTwisterFast mersenneTwisterFast)
    {

        double toReturn = Math.sin(2 * Math.PI * step / frequency);
        step++;
        return toReturn;

    }


    /**
     * Getter for property 'amplitude'.
     *
     * @return Value for property 'amplitude'.
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * Setter for property 'amplitude'.
     *
     * @param amplitude Value to set for property 'amplitude'.
     */
    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    /**
     * Getter for property 'frequency'.
     *
     * @return Value for property 'frequency'.
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Setter for property 'frequency'.
     *
     * @param frequency Value to set for property 'frequency'.
     */
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public DoubleParameter makeCopy() {
        SinusoidalDoubleParameter parameter = new SinusoidalDoubleParameter(amplitude, frequency);
        parameter.step=step;
        return parameter;
    }
}
