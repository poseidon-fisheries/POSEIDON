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

package uk.ac.ox.poseidon.agents.vessels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.tasks.travel.Factories.setDestinationToOrigin;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;

/**
 * A component-level product is built once per vessel component and kept in
 * the component's store; a vessel-level one, reached from a component's tree,
 * is built under a vessel scope and kept in the vessel's store. A
 * registration's teardown goes to the store that holds the product.
 */
class VesselComponentScopeTest {

  // Kept in fields: a scope holds its vessel weakly
  private final Vessel vessel = newVessel();
  private final VesselScope vesselScope = new VesselScope(
    mock(SimulationScope.class),
    vessel
  );
  private final ProductStore firstStore = new ProductStore();
  private final ProductStore secondStore = new ProductStore();
  private final VesselComponentScope first = new VesselComponentScope(
    vesselScope,
    firstStore
  );
  private final VesselComponentScope second = new VesselComponentScope(
    vesselScope,
    secondStore
  );
  /** What builds registered and teardowns have not removed. */
  private final List<CountingListener> registered = new ArrayList<>();
  private final VesselRegistry<CountingListener> registry = (
    registeredVessel,
    listener
  ) -> {
    registered.add(listener);
    return () -> registered.remove(listener);
  };

  private static Vessel newVessel() {
    final SimpleEventManager eventManager = new SimpleEventManager();
    final ProductStore store = new ProductStore();
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(store);
    when(vessel.getEventManager()).thenReturn(eventManager);
    return vessel;
  }

  @Test
  void aComponentLevelProductIsOnePerComponent() {
    final ComponentListenerFactory factory = new ComponentListenerFactory();

    assertThat(factory.get(first)).isSameAs(factory.get(first));
    assertThat(factory.get(first)).isNotSameAs(factory.get(second));
  }

  @Test
  void aTaskIsOnePerComponent() {
    final var task = setDestinationToOrigin();

    assertThat(task.get(first)).isSameAs(task.get(first));
    assertThat(task.get(first)).isNotSameAs(task.get(second));
  }

  @Test
  void aVesselLevelProductReachedFromComponentsIsOnePerVessel() {
    final ScopeClassFactory factory = new ScopeClassFactory();

    assertThat(factory.get(first))
      .isSameAs(factory.get(second))
      .isEqualTo(VesselScope.class);
  }

  @Test
  void aComponentsRegistrationGoesWhenTheComponentsStoreEnds() {
    final CountingListener listener = new ComponentListenerFactory().get(first);
    firstStore.end();
    vessel.getEventManager().broadcast("event");

    assertThat(listener.count).isZero();
  }

  @Test
  void aVesselLevelRegistrationReachedFromAComponentGoesWithTheVessel() {
    new RegisteringVesselListenerFactory().get(first);
    firstStore.end();

    assertThat(registered).hasSize(1);

    vessel.getProductStore().end();

    assertThat(registered).isEmpty();
  }

  @Test
  void vesselKnowledgeReachedFromTwoComponentsRegistersOnce() {
    final var factory = new RegisteringVesselListenerFactory();

    assertThat(factory.get(first)).isSameAs(factory.get(second));
    assertThat(registered).hasSize(1);
  }

  private static final class CountingListener extends AbstractListener<String> {

    private int count = 0;

    CountingListener() {
      super(String.class);
    }

    @Override
    public void receive(final String event) {
      count++;
    }
  }

  /** Registers its listener on the vessel's event manager when built. */
  private static final class ComponentListenerFactory
    extends VesselComponentScopeFactory<CountingListener>
  {

    @Override
    protected CountingListener newInstance(final VesselComponentScope scope) {
      final CountingListener listener = new CountingListener();
      scope.addListener(listener);
      return listener;
    }
  }

  /** Registers its listener with the registry when built. */
  private final class RegisteringVesselListenerFactory
    extends VesselScopeFactory<CountingListener>
  {

    @Override
    protected CountingListener newInstance(final VesselScope scope) {
      final CountingListener listener = new CountingListener();
      scope.register(registry, listener);
      return listener;
    }
  }

  /** Says the class of the scope its build received. */
  private static final class ScopeClassFactory
    extends VesselScopeFactory<Class<?>>
  {

    @Override
    protected Class<?> newInstance(final VesselScope scope) {
      return scope.getClass();
    }
  }
}
