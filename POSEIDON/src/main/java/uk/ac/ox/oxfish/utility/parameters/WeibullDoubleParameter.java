/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.WeibullDistribution;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

public class WeibullDoubleParameter implements DoubleParameter {

    private final double epsilon = 0.0001;
    private double shape;
    private double scale;
    private WeibullDistribution distribution;

    public WeibullDoubleParameter(double shape, double scale) {
        this.shape = Math.max(shape, epsilon); //Cannot be zero
        this.scale = Math.max(scale, epsilon);
    }

    @Override
    public DoubleParameter makeCopy() {
        return new WeibullDoubleParameter(shape, scale);
    }

    @Override
    public double applyAsDouble(MersenneTwisterFast mersenneTwisterFast) {

        if (distribution == null)
            distribution = new WeibullDistribution(
                new MTFApache(
                    mersenneTwisterFast
                ),
                shape, scale
            );

        return distribution.sample();
    }


    public double getShape() {
        return shape;
    }

    public void setShape(double shape) {
        this.shape = Math.max(shape, epsilon);
        distribution = null;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        distribution = null;
        this.scale = Math.max(scale, epsilon);
    }

    public double inverseCDF(double probability) {
        return (scale * Math.pow(-Math.log(1 - probability), 1 / shape));
    }
}
