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

import ec.util.MersenneTwisterFast;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

public class EpsilonGreedyChooser<O> implements Supplier<Optional<O>> {

    private final double epsilon;
    private final Picker<O> explorer;
    private final Picker<O> exploiter;
    private final MersenneTwisterFast rng;

    @SuppressFBWarnings(value = "EI2")
    EpsilonGreedyChooser(
        final double epsilon,
        final Picker<O> explorer,
        final Picker<O> exploiter,
        final MersenneTwisterFast rng
    ) {
        this.explorer = explorer;
        this.exploiter = exploiter;
        this.epsilon = checkUnitRange(epsilon, "epsilon");
        this.rng = checkNotNull(rng);
    }

    @Override
    public Optional<O> get() {
        final boolean explore = rng.nextBoolean(epsilon);
        return Optional
            .of(exploiter)
            .filter(__ -> !explore)
            .flatMap(Picker::pick)
            .or(explorer::pick);
    }
}
