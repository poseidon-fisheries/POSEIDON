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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CombiningEphemeralAccumulatingListenerTest {

  private List<Listener<?>> listeners;
  private EventManager eventManager;

  @BeforeEach
  void setUp() {
    listeners = new ArrayList<>();
    eventManager = new EventManager() {
      @Override
      public void addListener(final Listener<?> listener) {
        listeners.add(listener);
      }

      @Override
      public void removeListener(final Listener<?> listener) {
        listeners.remove(listener);
      }

      @Override
      public <E> void broadcast(final E event) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Test
  @SuppressWarnings("unchecked")
  void combinesTheValuesOfBothComponentListeners() {
    final CombiningEphemeralAccumulatingListener<
      Integer,
      Integer,
      String,
      Integer,
      String
    > combined = new CombiningEphemeralAccumulatingListener<>(
      eventManager,
      Integer.class,
      0,
      Integer::sum,
      String.class,
      0,
      (count, s) -> count + 1,
      (sum, count) -> sum + ":" + count
    );

    assertThat(listeners).hasSize(2);
    final EphemeralAccumulatingListener<Integer, Integer> first =
      (EphemeralAccumulatingListener<Integer, Integer>) listeners.get(0);
    final EphemeralAccumulatingListener<String, Integer> second =
      (EphemeralAccumulatingListener<String, Integer>) listeners.get(1);

    first.receive(5);
    first.receive(10);
    second.receive("a");
    second.receive("b");
    second.receive("c");

    assertThat(combined.get()).isEqualTo("15:3");
  }
}
