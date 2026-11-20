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

package uk.ac.ox.poseidon.core.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.events.Factories.eventClearer;
import static uk.ac.ox.poseidon.core.events.Factories.eventManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;

class FactoriesTest {

  private final Simulation simulation = mock(Simulation.class);
  private final SimulationScope scope = new SimulationScope(simulation);

  @BeforeEach
  void giveTheSimulationAProductStore() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
  }

  @Test
  void eventManagerResolvesToTheSimulationsEventManager() {
    final EventManager eventManager = mock(EventManager.class);
    when(simulation.getEventManager()).thenReturn(eventManager);

    assertThat(eventManager().get(scope)).isSameAs(eventManager);
  }

  @Test
  void eventClearerClearsTheResolvedAccumulator() {
    final EventAccumulator<String> accumulator = new EventAccumulator<>(
      String.class
    );
    accumulator.receive("a");
    final EventClearerFactory<SimulationScope> factory = eventClearer(
      _ -> accumulator
    );

    factory.get(scope).step(simulation);

    assertThat(accumulator.getEvents()).isEmpty();
  }

  @Test
  void aListenerFactoryRegistersItsListenerWhenBuilt() {
    final EventManager eventManager = mock(EventManager.class);
    when(simulation.getEventManager()).thenReturn(eventManager);

    final SimpleListener<String> listener = new StringListenerFactory().get(
      scope
    );

    verify(eventManager).addListener(listener);
  }

  private static final class StringListenerFactory
    extends SimulationEventListenerFactory<SimpleListener<String>>
  {

    @Override
    protected SimpleListener<String> newListener(final SimulationScope scope) {
      return new SimpleListener<>(String.class, _ -> {});
    }
  }
}
