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

package uk.ac.ox.poseidon.core.providers.random;

import ec.util.MersenneTwisterFast;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.providers.IntProvider;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link IntProvider} that returns a uniformly distributed random integer in the inclusive
 * range {@code [minimum, maximum]} on each call, drawn from the simulation's shared RNG. Built via
 * {@link Factories#randomInt(int, int)} in this package.
 */
public class RandomIntProvider implements IntProvider {

    private final @NonNull MersenneTwisterFast rng;
    private final int minimum;
    private final int range;

    /**
     * @param rng     the shared RNG to draw from
     * @param minimum the inclusive lower bound
     * @param maximum the inclusive upper bound, must be greater than or equal to {@code minimum}
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public RandomIntProvider(
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
