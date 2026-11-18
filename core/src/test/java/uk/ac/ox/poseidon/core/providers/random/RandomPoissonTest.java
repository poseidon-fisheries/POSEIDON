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

import static org.assertj.core.api.Assertions.assertThat;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import sim.util.distribution.Poisson;

/**
 * A random Poisson returns the next draw of the distribution it wraps.
 */
class RandomPoissonTest {

  @Test
  void returnsTheDistributionsNextValue() {
    final RandomPoisson random = new RandomPoisson(
      new Poisson(20, new MersenneTwisterFast(42))
    );
    final Poisson expected = new Poisson(20, new MersenneTwisterFast(42));

    assertThat(random.apply(null)).isPositive().isEqualTo(expected.nextInt());
  }
}
