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

package uk.ac.ox.poseidon.agents.behaviours.choices;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

public class RandomExplorer<O> implements Explorer<O> {

    private final ImmutableList<O> options;
    private final MersenneTwisterFast rng;

    public RandomExplorer(
        final List<O> options,
        final MersenneTwisterFast rng
    ) {
        this.options = ImmutableList.copyOf(options);
        this.rng = checkNotNull(rng);
    }

    @Override
    public O explore(final O currentOption) {
        return oneOf(options, rng);
    }
}
