/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.Well19937c;

public class WeibullDoubleParameter implements DoubleParameter {

    private double shape;

    private double scale;

    private WeibullDistribution distribution;

    public WeibullDoubleParameter(double shape, double scale)
    {
        this.shape = shape;
        this.scale = scale;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new WeibullDoubleParameter(shape,scale);
    }

    @Override
    public Double apply(MersenneTwisterFast mersenneTwisterFast) {

        if(distribution==null)
            distribution = new WeibullDistribution(
                    new Well19937c(
                            mersenneTwisterFast.nextLong()
                    ),
                    shape,scale);

        return distribution.sample();

    }


    public double getShape() {
        return shape;
    }

    public void setShape(double shape) {
        this.shape = shape;
        distribution=null;
    }

    public double getScale() {

        return scale;
    }

    public void setScale(double scale) {
        distribution=null;

        this.scale = scale;
    }
}
