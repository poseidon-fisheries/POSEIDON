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

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

/**
 * With probability {@code epsilon}, explores (calls {@link #explorer}); otherwise exploits
 * (calls {@link #exploiter}), falling back to exploring if the exploiter has nothing to offer.
 */
public class EpsilonGreedyChooser<O> implements Supplier<Optional<O>> {

    private final double epsilon;
    private final Supplier<O> explorer;
    private final Supplier<O> exploiter;
    private final MersenneTwisterFast rng;

    /**
     * @param epsilon   probability of exploring instead of exploiting, in {@code [0, 1]}
     * @param explorer  supplies an exploratory option
     * @param exploiter supplies the currently-best-known option
     * @param rng       the RNG to draw the explore/exploit decision from
     */
    EpsilonGreedyChooser(
        final double epsilon,
        final Supplier<O> explorer,
        final Supplier<O> exploiter,
        final MersenneTwisterFast rng
    ) {
        this.epsilon = checkUnitRange(epsilon, "epsilon");
        this.explorer = checkNotNull(explorer);
        this.exploiter = checkNotNull(exploiter);
        this.rng = checkNotNull(rng);
    }

    /** @return the exploiter's pick with probability {@code 1 - epsilon} (else the explorer's), falling back to the explorer if the exploiter returns {@code null} */
    @Override
    public Optional<O> get() {
        final boolean exploit = !rng.nextBoolean(epsilon);
        O result = null;
        if (exploit) {
            result = this.exploiter.get();
        }
        if (result == null) {
            result = this.explorer.get();
        }
        return Optional.ofNullable(result);
    }
}
