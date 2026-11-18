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
import static uk.ac.ox.poseidon.core.providers.random.Factories.rng;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/**
 * The entry points wire each provider to the simulation's shared RNG and keep
 * the parameters they are given.
 */
class FactoriesTest {

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation(42);
  private final SimulationScope scope = new SimulationScope(simulation);

  @Test
  void providesTheSimulationsOwnRandomNumberGenerator() {
    assertThat(rng().get(scope)).isSameAs(simulation.random);
  }

  @Test
  void theDefaultRandomBooleanIsEvenlyProbable() {
    final RandomBooleanFactory factory = Factories.randomBoolean();

    assertThat(factory.getProbability()).isEqualTo(0.5);
    assertThat(factory.get(scope)).isInstanceOf(RandomBoolean.class);
  }

  @Test
  void aRandomBooleanKeepsItsProbability() {
    final RandomBooleanFactory factory = Factories.randomBoolean(0.2);

    assertThat(factory.getProbability()).isEqualTo(0.2);
    assertThat(factory.get(scope)).isInstanceOf(RandomBoolean.class);
  }

  @Test
  void aRandomDoubleKeepsItsBounds() {
    final RandomDoubleFactory factory = Factories.randomDouble(2, 5);

    assertThat(factory.getMinimum()).isEqualTo(2);
    assertThat(factory.getMaximum()).isEqualTo(5);
    assertThat(factory.get(scope)).isInstanceOf(RandomDouble.class);
  }

  @Test
  void aRandomIntKeepsItsBounds() {
    final RandomIntFactory factory = Factories.randomInt(1, 6);

    assertThat(factory.getMinimum()).isEqualTo(1);
    assertThat(factory.getMaximum()).isEqualTo(6);
    assertThat(factory.get(scope)).isInstanceOf(RandomInt.class);
  }

  @Test
  void aRandomPoissonKeepsItsMean() {
    final RandomPoissonFactory factory = Factories.randomPoisson(3.5);

    assertThat(factory.getMean()).isEqualTo(3.5);
    assertThat(factory.get(scope)).isInstanceOf(RandomPoisson.class);
  }
}
