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

package uk.ac.ox.poseidon.core.time;

import ec.util.MersenneTwisterFast;
import lombok.RequiredArgsConstructor;
import sim.util.distribution.Exponential;

import java.time.Duration;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ExponentiallyDistributedDuration implements Supplier<Duration> {

    private final Exponential exponential;

    ExponentiallyDistributedDuration(
        final Duration meanDuration,
        final MersenneTwisterFast rng
    ) {
        this.exponential = new Exponential(1.0 / meanDuration.getSeconds(), rng);
    }

    @Override
    public Duration get() {
        return Duration.ofSeconds(
            1 + exponential.nextInt()
        );
    }

}
