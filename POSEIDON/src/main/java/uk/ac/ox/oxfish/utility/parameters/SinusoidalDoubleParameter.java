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

package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

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
    public double applyAsDouble(MersenneTwisterFast mersenneTwisterFast) {

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
        parameter.step = step;
        return parameter;
    }
}
