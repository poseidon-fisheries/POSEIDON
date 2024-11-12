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

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.MasonUtils;

import java.util.List;

@RequiredArgsConstructor
class RandomDestinationSupplier implements DestinationSupplier {

    @Getter private final ImmutableList<Int2D> possibleDestinations;
    private final MersenneTwisterFast rng;

    public RandomDestinationSupplier(
        List<Int2D> possibleDestinations,
        MersenneTwisterFast rng
    ) {
        this.possibleDestinations = ImmutableList.copyOf(possibleDestinations);
        this.rng = rng;
    }

    @Override
    public Int2D get() {
        return MasonUtils.oneOf(possibleDestinations, rng);
    }
}
