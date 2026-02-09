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

package uk.ac.ox.poseidon.core.suppliers;

import ec.util.MersenneTwisterFast;
import lombok.NonNull;

import java.util.function.IntSupplier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Supplies uniformly distributed random integers in the inclusive range [minimum, maximum].
 */
public class RandomIntSupplier implements IntSupplier {

    private final @NonNull MersenneTwisterFast rng;
    private final int minimum;
    private final int range;

    public RandomIntSupplier(
        @NonNull final MersenneTwisterFast rng,
        final int minimum,
        final int maximum
    ) {
        checkArgument(
            maximum >= minimum,
            "Maximum (%s) must be greater or equal to minimum (%s)".formatted(maximum, minimum)
        );
        final long rangeLong = (long) maximum - minimum + 1;
        checkArgument(
            rangeLong > 0 && rangeLong <= Integer.MAX_VALUE,
            "Range must be within integer bounds"
        );
        this.rng = rng;
        this.minimum = minimum;
        this.range = (int) rangeLong;
    }

    @Override
    public int getAsInt() {
        return minimum + rng.nextInt(range);
    }

}
