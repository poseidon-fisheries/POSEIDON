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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/**
 * A store ends with its owner, and an ended store refuses every later use; a
 * finished simulation, with the products in its store, goes once nothing
 * references it, even though its scenario lives on.
 */
class ProductStoreTest {

  private final ProductFactory factory = new ProductFactory();

  @Test
  void anEndedStoreRefusesEveryUse() {
    final ProductStore store = new ProductStore();
    store.put(factory, new Product(null));
    store.end();

    assertThatThrownBy(() -> store.contains(factory)).isInstanceOf(
      IllegalStateException.class
    );
    assertThatThrownBy(() -> store.get(factory)).isInstanceOf(
      IllegalStateException.class
    );
    assertThatThrownBy(() ->
      store.put(factory, new Product(null))
    ).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(store::end).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void aFinishedSimulationRefusesToBuildMore() {
    final Simulation simulation = scenario().startNewSimulation();
    simulation.finish();

    assertThatThrownBy(() ->
      new ProductFactory().get(new SimulationScope(simulation))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void aFinishedSimulationCanBeCollectedWhileItsScenarioLives()
    throws InterruptedException {
    final Scenario scenario = scenario();
    final WeakReference<Simulation> simulation = finishedSimulationOf(scenario);

    assertThat(isCollected(simulation)).isTrue();
    Reference.reachabilityFence(scenario);
  }

  private Scenario scenario() {
    return Scenario.builder()
      .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
      .component("product", factory)
      .build();
  }

  private static WeakReference<Simulation> finishedSimulationOf(
    final Scenario scenario
  ) {
    final Simulation simulation = scenario.startNewSimulation();
    simulation.finish();
    return new WeakReference<>(simulation);
  }

  @SuppressFBWarnings(
    value = "DM_GC",
    justification = "Collection is what the tests calling this check."
  )
  static boolean isCollected(final WeakReference<?> reference)
    throws InterruptedException {
    for (int i = 0; i < 100 && reference.get() != null; i++) {
      System.gc();
      Thread.sleep(10);
    }
    return reference.get() == null;
  }

  /** Holds its simulation, which goes only with the store keeping it. */
  record Product(Simulation simulation) {}

  static final class ProductFactory extends SimulationScopeFactory<Product> {

    @Override
    protected Product newInstance(final SimulationScope scope) {
      return new Product(scope.getSimulation());
    }
  }
}
