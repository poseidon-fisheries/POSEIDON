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
import static uk.ac.ox.poseidon.agents.vessels.DeclaredComponents.componentScopeOf;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselEventListener;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;

class VesselEventListenerFactoryTest {

  // Kept in fields: a scope holds its vessel weakly
  private final Vessel vesselA = newVessel();
  private final Vessel vesselB = newVessel();
  private final ProductStore componentStoreA = new ProductStore();
  private final VesselComponentScope scopeA = new VesselComponentScope(
    scopeOf(vesselA),
    componentStoreA
  );
  private final VesselComponentScope scopeB = componentScopeOf(
    scopeOf(vesselB)
  );
  private final CountingListenerFactory listener =
    new CountingListenerFactory();
  private final VesselEventListenerFactory<CountingListener> factory =
    vesselEventListener(listener);

  private static Vessel newVessel() {
    final EventManager eventManager = new SimpleEventManager();
    final ProductStore store = new ProductStore();
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(store);
    when(vessel.getEventManager()).thenReturn(eventManager);
    return vessel;
  }

  private static VesselScope scopeOf(final Vessel vessel) {
    return new VesselScope(mock(SimulationScope.class), vessel);
  }

  private static class CountingListener extends AbstractListener<String> {

    private int count = 0;

    CountingListener() {
      super(String.class);
    }

    @Override
    public void receive(final String event) {
      count++;
    }
  }

  private static class CountingListenerFactory
    extends VesselScopeFactory<CountingListener>
  {

    @Override
    protected CountingListener newInstance(final VesselScope scope) {
      return new CountingListener();
    }
  }

  private static void broadcastOn(final Vessel vessel) {
    vessel.getEventManager().broadcast("event");
  }

  @Test
  void registersTheListenerOnTheVesselsEventManager() {
    factory.get(scopeA);
    broadcastOn(vesselA);

    assertThat(listener.get(scopeA).count).isEqualTo(1);
  }

  @Test
  void removesTheListenerWhenTheComponentsStoreEnds() {
    final CountingListener vesselListener = factory.get(scopeA);
    componentStoreA.end();
    broadcastOn(vesselA);

    assertThat(vesselListener.count).isZero();
  }

  @Test
  void registersTheListenerOnlyOnceForAVessel() {
    factory.get(scopeA);
    factory.get(scopeA);
    broadcastOn(vesselA);

    assertThat(listener.get(scopeA).count).isEqualTo(1);
  }

  @Test
  void registersEachVesselsListenerOnItsOwnEventManager() {
    factory.get(scopeA);
    factory.get(scopeB);
    broadcastOn(vesselB);

    assertThat(listener.get(scopeA).count).isZero();
    assertThat(listener.get(scopeB).count).isEqualTo(1);
  }

  @Test
  void givesTheListenerTheListenersFactoryGivesForTheVessel() {
    assertThat(factory.get(scopeA)).isNotNull().isSameAs(listener.get(scopeA));
  }
}
