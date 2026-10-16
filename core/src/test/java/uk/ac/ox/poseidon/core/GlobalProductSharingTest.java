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
import static uk.ac.ox.poseidon.core.ProductStoreTest.isCollected;
import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;

/**
 * Global products are built once and shared by every simulation of a
 * scenario, and by scenarios derived from it that keep the same factory, so
 * that expensive read-only products and pure caches (grids, path caches) are
 * not rebuilt for every run. Sharing is by identity: a copy of a factory, as
 * derivation makes, builds its own product, however equal it is to the
 * original. A global product is kept by the factory that built it, so a
 * dropped factory goes even while its inputs live on.
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
    final Scenario derivedScenario = scenario.withComponent(
      "other",
      new ObjectFactory<>("another component")
    );
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

  @Test
  void aCopyOfAGlobalFactoryBuildsItsOwnProduct() {
    final NewObjectGlobalFactory factory = new NewObjectGlobalFactory();
    final NewObjectGlobalFactory copy = factory.toBuilder().build();

    assertThat(copy).isEqualTo(factory);
    assertThat(copy.get(GLOBAL_SCOPE)).isNotSameAs(factory.get(GLOBAL_SCOPE));
  }

  @Test
  void aRelativeFactoryOverAGlobalInputCanBeCollectedWhileTheInputLives()
    throws InterruptedException {
    final NewObjectGlobalFactory input = new NewObjectGlobalFactory();
    final WeakReference<NewObjectRelativeFactory<Scope>> factory = builtOver(
      input
    );

    assertThat(isCollected(factory)).isTrue();
    Reference.reachabilityFence(input);
  }

  private static WeakReference<NewObjectRelativeFactory<Scope>> builtOver(
    final NewObjectGlobalFactory input
  ) {
    final NewObjectRelativeFactory<Scope> factory =
      new NewObjectRelativeFactory<>(input);
    factory.get(GLOBAL_SCOPE);
    return new WeakReference<>(factory);
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

  @NoArgsConstructor
  @SuperBuilder(toBuilder = true)
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

  @Getter
  @AllArgsConstructor
  static final class NewObjectRelativeFactory<S extends Scope> extends RelativeScopeFactory<
    S,
    Product
  > {

    private final Factory<? super S, ?> input;

    @Override
    protected Product newInstance(final S scope) {
      return new Product();
    }
  }
}
