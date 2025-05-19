/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

/**
 * A decorator class for {@link DoubleParameter}, which adds a scaling factor
 * by which the parameter value is multiplied.
 */
public class ScaledDoubleParameter implements DoubleParameter {

    private DoubleParameter delegate;
    private double scalingFactor = 1;

    public ScaledDoubleParameter() {
    }

    public ScaledDoubleParameter(final DoubleParameter delegate, final double scalingFactor) {
        this.delegate = delegate;
        this.scalingFactor = scalingFactor;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new ScaledDoubleParameter(getDelegate(), getScalingFactor());
    }

    public DoubleParameter getDelegate() {
        return delegate;
    }

    public void setDelegate(final DoubleParameter delegate) {
        this.delegate = delegate;
    }

    public double getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(final double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    @Override
    public double applyAsDouble(final MersenneTwisterFast rng) {
        return delegate.applyAsDouble(rng) * getScalingFactor();
    }
}
