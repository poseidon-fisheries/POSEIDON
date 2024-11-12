/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.destination;

import ec.util.MersenneTwisterFast;
import sim.util.Int2D;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

class EpsilonGreedyDestinationSupplier implements DestinationSupplier {

    private final double epsilon;
    private final Supplier<Int2D> greedyDestinationSupplier;
    private final Supplier<Int2D> nonGreedyDestinationSupplier;
    private final MersenneTwisterFast rng;

    EpsilonGreedyDestinationSupplier(
        final double epsilon,
        final Supplier<Int2D> greedyDestinationSupplier,
        final Supplier<Int2D> nonGreedyDestinationSupplier,
        final MersenneTwisterFast rng
    ) {
        checkArgument(
            epsilon >= 0 && epsilon <= 1,
            "epsilon must be between 0 and 1"
        );
        this.epsilon = epsilon;
        this.greedyDestinationSupplier = checkNotNull(greedyDestinationSupplier);
        this.nonGreedyDestinationSupplier = checkNotNull(nonGreedyDestinationSupplier);
        this.rng = checkNotNull(rng);
    }

    @Override
    public Int2D get() {
        return rng.nextBoolean(epsilon)
            ? greedyDestinationSupplier.get()
            : nonGreedyDestinationSupplier.get();
    }
}
