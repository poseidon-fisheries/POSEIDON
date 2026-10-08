/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.choices;

import java.util.function.BinaryOperator;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

/**
 * A {@link Memory} update rule for {@code Double}s: an exponential moving average, which
 * remembers {@code recollection × (1 − alpha) + observation × alpha}. An {@code alpha} of 1
 * replaces what is remembered with each new observation; an {@code alpha} of 0 keeps it forever.
 */
public class ExponentialMovingAverageOfDoubles implements BinaryOperator<Double> {

    private final double alpha;

    /** @param alpha the weight of a new observation, between 0 and 1 */
    ExponentialMovingAverageOfDoubles(final double alpha) {
        this.alpha = checkUnitRange(alpha, "alpha");
    }

    @Override
    public Double apply(
        final Double recollection,
        final Double observation
    ) {
        return recollection * (1 - alpha) + observation * alpha;
    }
}
