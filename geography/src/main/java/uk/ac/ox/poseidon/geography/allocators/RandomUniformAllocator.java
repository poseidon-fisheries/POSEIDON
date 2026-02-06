/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.allocators;

import ec.util.MersenneTwisterFast;
import lombok.NonNull;
import sim.util.Int2D;

import static com.google.common.base.Preconditions.checkArgument;

public class RandomUniformAllocator implements Allocator {

    private final @NonNull MersenneTwisterFast rng;
    private final double minimum;
    private final double maximum;

    public RandomUniformAllocator(
        final @NonNull MersenneTwisterFast rng,
        final double minimum,
        final double maximum
    ) {
        checkArgument(
            minimum < maximum,
            "minimum must be less than maximum"
        );
        this.rng = rng;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public double applyAsDouble(final Int2D cell) {
        return minimum + rng.nextDouble() * (maximum - minimum);
    }
}
