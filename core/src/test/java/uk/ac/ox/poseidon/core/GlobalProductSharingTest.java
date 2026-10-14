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

package uk.ac.ox.poseidon.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;

/**
 * Global products are built once and shared by every simulation of a
 * scenario, and by scenarios derived from it that keep the same factory
 * (R4 in the factory design brief).
 */
class GlobalProductSharingTest {

  @Test
  void simulationsOfOneScenarioShareGlobalProducts() {
    final Scenario scenario = scenarioWith(new NewObjectGlobalFactory());
    final Simulation first = scenario.startNewSimulation();
    final Simulation second = scenario.startNewSimulation();
    try {
      assertThat(second.getComponent(Product.class)).isSameAs(
        first.getComponent(Product.class)
      );
    } finally {
      first.finish();
      second.finish();
    }
  }

  @Test
  void aDerivedScenarioSharesTheGlobalProductsOfFactoriesItKeeps() {
    final Scenario scenario = scenarioWith(new NewObjectGlobalFactory());
    final Scenario derivedScenario = scenario
      .toBuilder()
      .component("other", new ObjectFactory<>("another component"))
      .build();
    final Simulation first = scenario.startNewSimulation();
    final Simulation second = derivedScenario.startNewSimulation();
    try {
      assertThat(second.getComponent(Product.class)).isSameAs(
        first.getComponent(Product.class)
      );
    } finally {
      first.finish();
      second.finish();
    }
  }

  @Test
  void simulationsOfOneScenarioDoNotShareSimulationProducts() {
    final Scenario scenario = scenarioWith(new NewObjectSimulationFactory());
    final Simulation first = scenario.startNewSimulation();
    final Simulation second = scenario.startNewSimulation();
    try {
      assertThat(second.getComponent(Product.class)).isNotSameAs(
        first.getComponent(Product.class)
      );
    } finally {
      first.finish();
      second.finish();
    }
  }

  private static Scenario scenarioWith(
    final Factory<? super SimulationScope, ?> factory
  ) {
    return Scenario.builder()
      .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
      .component("product", factory)
      .build();
  }

  static final class Product {}

  static final class NewObjectGlobalFactory
    extends GlobalScopeFactory<Product>
  {

    @Override
    protected Product newInstance(final Scope scope) {
      return new Product();
    }
  }

  static final class NewObjectSimulationFactory
    extends SimulationScopeFactory<Product>
  {

    @Override
    protected Product newInstance(final SimulationScope scope) {
      return new Product();
    }
  }
}
