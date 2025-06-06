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

package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

/**
 * A uniformly distributed double will be returned each time
 * <p>
 * Created by carrknight on 6/7/15.
 */
public class UniformDoubleParameter implements DoubleParameter {

    private double minimum;

    private double maximum;

    public UniformDoubleParameter() {
    }

    public UniformDoubleParameter(
        double minimum,
        double maximum
    ) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public double applyAsDouble(MersenneTwisterFast mersenneTwisterFast) {
        Preconditions.checkArgument(maximum >= minimum, "maximum is not bigger than minimum");
        return (maximum - minimum) * mersenneTwisterFast.nextDouble(true, true) + minimum;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    @Override
    public UniformDoubleParameter makeCopy() {
        return new UniformDoubleParameter(minimum, maximum);
    }
}
