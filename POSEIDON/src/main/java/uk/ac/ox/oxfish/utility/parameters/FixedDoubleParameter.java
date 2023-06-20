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

/**
 * The parameter returned is always the same
 * Created by carrknight on 6/7/15.
 */
public class FixedDoubleParameter extends FixedParameter<Double> implements DoubleParameter {

    public FixedDoubleParameter(final double value) {
        super(value);
    }

    public FixedDoubleParameter(final Double value) {
        super(value);
    }

    public void setValue(final double value) {
        super.setValue(value);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public double applyAsDouble(final MersenneTwisterFast mersenneTwisterFast) {
        return getValue();
    }

    @Override
    public FixedDoubleParameter makeCopy() {
        return new FixedDoubleParameter(getValue());
    }
}
