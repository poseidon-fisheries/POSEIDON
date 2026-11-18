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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;

import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

class EventClearerTest {

  @Test
  void stepClearsTheAccumulator() {
    final EventAccumulator<String> accumulator = new EventAccumulator<>(
      String.class
    );
    accumulator.receive("a");
    accumulator.receive("b");
    final EventClearer clearer = new EventClearer(accumulator);

    clearer.step(mock(SimState.class));

    assertThat(accumulator.getEvents()).isEmpty();
  }

  @Test
  void factoryBuildsAClearerOverTheResolvedAccumulator() {
    final EventAccumulator<String> accumulator = new EventAccumulator<>(
      String.class
    );
    accumulator.receive("a");
    final EventClearer clearer = EventClearerFactory.<Scope>builder()
      .eventAccumulator(new HeldAccumulator<>(accumulator))
      .build()
      .get(GLOBAL_SCOPE);

    clearer.step(mock(SimState.class));

    assertThat(accumulator.getEvents()).isEmpty();
  }

  private static final class HeldAccumulator<E> extends GlobalScopeFactory<
    EventAccumulator<E>
  > {

    private final EventAccumulator<E> accumulator;

    HeldAccumulator(final EventAccumulator<E> accumulator) {
      this.accumulator = accumulator;
    }

    @Override
    protected EventAccumulator<E> newInstance(final Scope scope) {
      return accumulator;
    }
  }
}
