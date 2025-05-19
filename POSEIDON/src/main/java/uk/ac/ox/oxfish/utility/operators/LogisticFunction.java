/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.operators;

import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.exp;

public class LogisticFunction implements DoubleUnaryOperator {

    private final double midpoint;
    private final double steepness;
    private final double maximum;

    public LogisticFunction(
        final double midpoint,
        final double steepness
    ) {
        this(midpoint, steepness, 1.0);
    }

    public LogisticFunction(
        final double midpoint,
        final double steepness,
        final double maximum
    ) {
        this.midpoint = midpoint;
        this.steepness = steepness;
        this.maximum = maximum;
    }

    @Override
    public double applyAsDouble(final double operand) {
        return maximum / (1 + exp(-steepness * (operand - midpoint)));
    }

}
