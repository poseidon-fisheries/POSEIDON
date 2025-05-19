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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

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
        return new ConditionalDoubleParameter(active, value.makeCopy());
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public double applyAsDouble(MersenneTwisterFast mersenneTwisterFast) {
        if (active)
            return value.applyAsDouble(mersenneTwisterFast);
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
